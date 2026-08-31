package com.assignment.fileuploadpolicy.domain.upload.dto;

import com.assignment.fileuploadpolicy.domain.upload.entity.UploadedFile;
import java.util.List;
import org.springframework.data.domain.Page;

public record UploadHistoryResponse(
        List<UploadHistoryItemResponse> items,
        int totalCount,
        boolean hasMore
) {
    public static UploadHistoryResponse from(Page<UploadedFile> page) {
        List<UploadHistoryItemResponse> items = page.getContent().stream()
                .map(UploadHistoryItemResponse::from)
                .toList();
        return new UploadHistoryResponse(items, (int) page.getTotalElements(), page.hasNext());
    }
}