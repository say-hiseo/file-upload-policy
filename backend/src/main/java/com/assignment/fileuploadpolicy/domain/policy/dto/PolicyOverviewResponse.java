package com.assignment.fileuploadpolicy.domain.policy.dto;

import java.util.List;

public record PolicyOverviewResponse(
        List<ExtensionPolicyResponse> fixed,
        List<ExtensionPolicyResponse> custom,
        int customCount,
        int customMax
) {
    public static PolicyOverviewResponse from(PolicyOverview overview) {
        List<ExtensionPolicyResponse> fixed = overview.fixedPolicies().stream()
                .map(ExtensionPolicyResponse::from)
                .toList();
        List<ExtensionPolicyResponse> custom = overview.customPolicies().stream()
                .map(ExtensionPolicyResponse::from)
                .toList();
        return new PolicyOverviewResponse(fixed, custom, overview.customCount(), overview.customMax());
    }
}