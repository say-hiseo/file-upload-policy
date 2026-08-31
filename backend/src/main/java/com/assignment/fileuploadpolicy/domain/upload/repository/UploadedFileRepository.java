package com.assignment.fileuploadpolicy.domain.upload.repository;

import com.assignment.fileuploadpolicy.domain.upload.entity.UploadStatus;
import com.assignment.fileuploadpolicy.domain.upload.entity.UploadedFile;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    Page<UploadedFile> findAllByUploadedByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long memberId, Pageable pageable);

    Optional<UploadedFile> findByIdAndDeletedAtIsNull(Long id);
}