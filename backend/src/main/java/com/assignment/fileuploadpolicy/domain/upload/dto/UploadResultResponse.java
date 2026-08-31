package com.assignment.fileuploadpolicy.domain.upload.dto;

import com.assignment.fileuploadpolicy.domain.upload.entity.UploadedFile;
import com.assignment.fileuploadpolicy.domain.upload.entity.UploadStatus;

public record UploadResultResponse(
        String filename,
        UploadStatus status,
        String reason
) {
    public static UploadResultResponse from(UploadedFile file) {
        return new UploadResultResponse(file.getOriginalFilename(), file.getStatus(), file.getRejectReason());
    }
}