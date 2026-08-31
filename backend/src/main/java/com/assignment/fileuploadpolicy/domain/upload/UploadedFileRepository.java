package com.assignment.fileuploadpolicy.domain.upload;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {

    Page<UploadedFile> findAllByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

    Page<UploadedFile> findAllByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
            UploadStatus status, Pageable pageable);

    /**
     * 로그인 사용자 본인의 업로드 이력 조회. (신규 요구사항: 과거 파일 재조회/재다운로드)
     */
    Page<UploadedFile> findAllByUploadedByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long memberId, Pageable pageable);

    Optional<UploadedFile> findByIdAndDeletedAtIsNull(Long id);
}