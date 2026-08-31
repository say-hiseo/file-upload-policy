package com.assignment.fileuploadpolicy.domain.upload.dto;

import com.assignment.fileuploadpolicy.domain.upload.UploadStatus;
import com.assignment.fileuploadpolicy.domain.upload.UploadedFile;
import java.time.OffsetDateTime;

public record UploadHistoryItemResponse(
        Long id,
        String originalFilename,
        UploadStatus status,
        String rejectReason,
        long sizeBytes,
        OffsetDateTime createdAt,
        boolean downloadable
) {
    public static UploadHistoryItemResponse from(UploadedFile file) {
        boolean downloadable = file.getStatus() == UploadStatus.SUCCESS
                && file.getStoredFilename() != null;
        return new UploadHistoryItemResponse(
                file.getId(), file.getOriginalFilename(), file.getStatus(),
                file.getRejectReason(), file.getSizeBytes(), file.getCreatedAt(), downloadable);
    }
}