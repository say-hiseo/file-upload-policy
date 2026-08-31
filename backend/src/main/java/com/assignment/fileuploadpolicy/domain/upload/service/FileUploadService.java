package com.assignment.fileuploadpolicy.domain.upload.service;

import com.assignment.fileuploadpolicy.domain.policy.entity.ExtensionPolicy;
import com.assignment.fileuploadpolicy.domain.policy.repository.ExtensionPolicyRepository;
import com.assignment.fileuploadpolicy.domain.policy.service.ExtensionNormalizer;
import com.assignment.fileuploadpolicy.domain.upload.dto.FileDownload;
import com.assignment.fileuploadpolicy.domain.upload.entity.UploadStatus;
import com.assignment.fileuploadpolicy.domain.upload.entity.UploadedFile;
import com.assignment.fileuploadpolicy.domain.upload.repository.UploadedFileRepository;
import com.assignment.fileuploadpolicy.global.auth.ActorContext;
import com.assignment.fileuploadpolicy.global.config.UploadProperties;
import com.assignment.fileuploadpolicy.global.exception.BusinessException;
import com.assignment.fileuploadpolicy.global.exception.ErrorCode;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private static final int MAX_FILENAME_LENGTH = 255;

    private final ExtensionPolicyRepository extensionPolicyRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final MagicByteValidator magicByteValidator;
    private final UploadProperties uploadProperties;

    @Transactional
    public List<UploadedFile> upload(List<MultipartFile> files, ActorContext actor) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "업로드할 파일이 없습니다");
        }
        if (files.size() > uploadProperties.maxFileCountPerRequest()) {
            throw new BusinessException(ErrorCode.TOO_MANY_FILES_IN_REQUEST,
                    uploadProperties.maxFileCountPerRequest());
        }

        Set<String> blockedExtensions = extensionPolicyRepository.findByBlockedTrue().stream()
                .map(ExtensionPolicy::getExtension)
                .collect(Collectors.toSet());

        return files.stream()
                .map(file -> processSingleFile(file, blockedExtensions, actor))
                .toList();
    }

    private UploadedFile processSingleFile(MultipartFile file, Set<String> blockedExtensions, ActorContext actor) {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("(알 수 없음)");
        String detectedMime = file.getContentType();
        String representativeExtension = resolveRepresentativeExtension(originalFilename);

        String rejectReason = validate(file, originalFilename, blockedExtensions);
        if (rejectReason != null) {
            log.warn("업로드 거부 - filename={}, reason={}", originalFilename, rejectReason);
            return uploadedFileRepository.save(UploadedFile.rejected(
                    originalFilename, representativeExtension, detectedMime, null,
                    file.getSize(), rejectReason, actor.memberId(), actor.username()));
        }

        if (magicByteValidator.isDangerousSignature(file)) {
            log.warn("업로드 거부(매직바이트) - filename={}", originalFilename);
            return uploadedFileRepository.save(UploadedFile.rejected(
                    originalFilename, representativeExtension, detectedMime, true, file.getSize(),
                    ErrorCode.DANGEROUS_FILE_SIGNATURE.formatMessage(),
                    actor.memberId(), actor.username()));
        }

        String storedFilename = storeToDisk(file, representativeExtension);
        return uploadedFileRepository.save(UploadedFile.success(
                originalFilename, storedFilename, representativeExtension, detectedMime, false,
                file.getSize(), actor.memberId(), actor.username()));
    }

    private String validate(MultipartFile file, String originalFilename, Set<String> blockedExtensions) {
        if (file.isEmpty()) {
            return ErrorCode.EMPTY_FILE.formatMessage();
        }
        if (originalFilename.length() > MAX_FILENAME_LENGTH) {
            return ErrorCode.FILENAME_TOO_LONG.formatMessage(MAX_FILENAME_LENGTH);
        }

        List<String> rawFragments = FileNameParser.extractExtensionFragments(originalFilename);
        for (String rawFragment : rawFragments) {
            Optional<String> normalized = ExtensionNormalizer.normalize(rawFragment);
            if (normalized.isPresent() && blockedExtensions.contains(normalized.get())) {
                return ErrorCode.FILE_EXTENSION_BLOCKED.formatMessage(normalized.get());
            }
        }
        return null;
    }

    private String resolveRepresentativeExtension(String originalFilename) {
        List<String> fragments = FileNameParser.extractExtensionFragments(originalFilename);
        if (fragments.isEmpty()) {
            return null;
        }
        String last = fragments.get(fragments.size() - 1);
        return ExtensionNormalizer.normalize(last).orElse(last.toLowerCase(Locale.ROOT));
    }

    private String storeToDisk(MultipartFile file, String extension) {
        try {
            Path storageDir = Path.of(uploadProperties.storagePath());
            Files.createDirectories(storageDir);

            String storedFilename = UUID.randomUUID() + (extension != null ? "." + extension : "");
            Path target = storageDir.resolve(storedFilename);
            file.transferTo(target);
            return storedFilename;
        } catch (IOException e) {
            log.error("파일 저장 실패", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public Page<UploadedFile> getHistory(ActorContext actor, Pageable pageable) {
        requireLogin(actor);
        return uploadedFileRepository.findAllByUploadedByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                actor.memberId(), pageable);
    }

    @Transactional(readOnly = true)
    public FileDownload loadForDownload(Long uploadedFileId, ActorContext actor) {
        requireLogin(actor);

        UploadedFile file = uploadedFileRepository.findByIdAndDeletedAtIsNull(uploadedFileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UPLOAD_FILE_NOT_FOUND));

        if (file.getStatus() != UploadStatus.SUCCESS || file.getStoredFilename() == null) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_NOT_FOUND);
        }
        if (!actor.memberId().equals(file.getUploadedByMemberId())) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_FORBIDDEN);
        }

        Path target = Path.of(uploadProperties.storagePath()).resolve(file.getStoredFilename());
        Resource resource;
        try {
            resource = new UrlResource(target.toUri());
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.UPLOAD_FILE_NOT_FOUND);
        }
        if (!resource.exists() || !resource.isReadable()) {
            log.error("DB에는 있으나 디스크에서 찾을 수 없는 파일 - id={}, storedFilename={}",
                    file.getId(), file.getStoredFilename());
            throw new BusinessException(ErrorCode.UPLOAD_FILE_NOT_FOUND);
        }

        return new FileDownload(resource, file.getOriginalFilename());
    }

    private void requireLogin(ActorContext actor) {
        if (actor.memberId() == null) {
            throw new BusinessException(ErrorCode.UPLOAD_LOGIN_REQUIRED);
        }
    }
}