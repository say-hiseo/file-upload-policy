package com.assignment.fileuploadpolicy.domain.upload.dto;

import com.assignment.fileuploadpolicy.domain.upload.entity.UploadedFile;
import java.util.List;

public record UploadResponse(List<UploadResultResponse> results) {
    public static UploadResponse from(List<UploadedFile> files) {
        return new UploadResponse(files.stream().map(UploadResultResponse::from).toList());
    }
}