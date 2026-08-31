package com.assignment.fileuploadpolicy.domain.policy.dto;

import com.assignment.fileuploadpolicy.domain.policy.PolicyOverview;
import java.util.List;

/**
 * 요구사항 화면 구조를 그대로 반영한 응답 형태.
 *   fixed: 고정 확장자 7개 + 체크 상태
 *   custom: 커스텀 확장자 목록
 *   customCount / customMax: "3/200" 표시용
 */
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