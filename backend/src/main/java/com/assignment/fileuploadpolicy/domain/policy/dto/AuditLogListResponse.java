package com.assignment.fileuploadpolicy.domain.policy.dto;

import com.assignment.fileuploadpolicy.domain.policy.entity.ExtensionPolicyAuditLog;
import java.util.List;
import org.springframework.data.domain.Page;

public record AuditLogListResponse(
        List<AuditLogItemResponse> items,
        int totalCount,
        boolean hasMore
) {
    public static AuditLogListResponse from(Page<ExtensionPolicyAuditLog> page) {
        List<AuditLogItemResponse> items = page.getContent().stream()
                .map(AuditLogItemResponse::from)
                .toList();
        return new AuditLogListResponse(items, (int) page.getTotalElements(), page.hasNext());
    }
}