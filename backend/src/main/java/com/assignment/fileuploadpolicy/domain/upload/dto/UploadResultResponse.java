package com.assignment.fileuploadpolicy.domain.upload.dto;

import com.assignment.fileuploadpolicy.domain.upload.UploadedFile;
import com.assignment.fileuploadpolicy.domain.upload.UploadStatus;

public record UploadResultResponse(
        String filename,
        UploadStatus status,
        String reason
) {
    public static UploadResultResponse from(UploadedFile file) {
        return new UploadResultResponse(file.getOriginalFilename(), file.getStatus(), file.getRejectReason());
    }
}