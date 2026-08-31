package com.assignment.fileuploadpolicy.domain.upload;

import com.assignment.fileuploadpolicy.domain.policy.ExtensionNormalizer;
import com.assignment.fileuploadpolicy.domain.policy.ExtensionPolicy;
import com.assignment.fileuploadpolicy.domain.policy.ExtensionPolicyRepository;
import com.assignment.fileuploadpolicy.global.auth.ActorContext;
import com.assignment.fileuploadpolicy.global.config.UploadProperties;
import com.assignment.fileuploadpolicy.global.exception.BusinessException;
import com.assignment.fileuploadpolicy.global.exception.ErrorCode;
import java.io.IOException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * B. 실제 파일 업로드 처리.
 * A(정책 화면)와 동일한 조회 지점(ExtensionPolicyRepository.findByBlockedTrue)을 사용해,
 * 정책이 실제 업로드에도 일관되게 강제되도록 한다. (요구사항 문서 "A와 B가 같은 쿼리를
 * 공유해야 하는 이유" 참고)
 *
 * 요청 레벨 검증(개수 초과 등)은 예외를 던져 요청 전체를 거부하고,
 * 파일별 검증(확장자 차단, 매직바이트 등)은 예외 없이 결과값으로 모아
 * "부분 성공" 응답을 만든다. (CONSIDERATIONS.md 1-6 참고)
 */
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
        String detectedMime = file.getContentType(); // 신뢰하지 않음, 기록용 (CONSIDERATIONS.md 1-8)
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

    /**
     * 디스크에 쓰기 전에 완료되는 사전 검증. 실패 사유를 반환하고,
     * 통과하면 null을 반환한다 (1-8 "임시 파일 잔여 방지" 원칙).
     */
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
            // 정규화 실패 조각은 화이트리스트를 벗어난 형식이라 "알려진 차단 확장자"와
            // 애초에 일치할 수 없다 - 이 조각만 건너뛰고 파일 전체를 거부하진 않는다.
        }
        return null;
    }

    /**
     * 화면/기록에 표시할 대표 확장자 (마지막 조각). 실제 차단 판단은 validate()가
     * 모든 조각을 대상으로 이미 수행했으므로, 여기서는 표시값 하나만 정한다.
     */
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

    /**
     * 로그인 사용자 본인의 업로드 이력 조회. (신규 요구사항)
     * 비로그인 상태는 명시적으로 거부한다 - 남의 이력이 SYSTEM 계정으로
     * 뒤섞여 노출되는 걸 방지하기 위해, "본인 확인이 되는 경우에만" 응답한다.
     */
    @Transactional(readOnly = true)
    public Page<UploadedFile> getHistory(ActorContext actor, Pageable pageable) {
        requireLogin(actor);
        return uploadedFileRepository.findAllByUploadedByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                actor.memberId(), pageable);
    }

    /**
     * 파일 다운로드. 소유권 검증(본인 파일만) 후 리소스를 반환한다.
     * (CONSIDERATIONS.md 1-7에서 계획했던 "인증 기반 다운로드 API"의 실제 구현체)
     */
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