package com.assignment.fileuploadpolicy.domain.policy.dto;

import com.assignment.fileuploadpolicy.domain.policy.entity.AuditAction;
import com.assignment.fileuploadpolicy.domain.policy.entity.ExtensionPolicyAuditLog;
import com.assignment.fileuploadpolicy.domain.policy.entity.PolicyType;
import java.time.OffsetDateTime;

public record AuditLogItemResponse(
        OffsetDateTime changedAt,
        String changedByUsername,
        AuditAction action,
        String extension,
        PolicyType type
) {
    public static AuditLogItemResponse from(ExtensionPolicyAuditLog log) {
        return new AuditLogItemResponse(log.getChangedAt(), log.getChangedByUsername(),
                log.getAction(), log.getExtension(), log.getType());
    }
}