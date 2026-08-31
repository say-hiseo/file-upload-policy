package com.assignment.fileuploadpolicy.domain.policy.dto;

import com.assignment.fileuploadpolicy.domain.policy.entity.ExtensionPolicy;

public record ExtensionPolicyResponse(
        String extension,
        boolean blocked
) {
    public static ExtensionPolicyResponse from(ExtensionPolicy policy) {
        return new ExtensionPolicyResponse(policy.getExtension(), policy.isBlocked());
    }
}