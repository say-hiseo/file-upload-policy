package com.assignment.fileuploadpolicy.domain.policy.dto;

import com.assignment.fileuploadpolicy.domain.policy.ExtensionPolicy;

/**
 * 엔티티를 그대로 컨트롤러 응답으로 내보내지 않기 위한 응답 DTO.
 * (엔티티가 API 스펙에 종속되면, 나중에 엔티티 구조를 바꿀 때 API 계약까지
 * 같이 깨지는 문제가 생기므로 분리한다)
 */
public record ExtensionPolicyResponse(
        String extension,
        boolean blocked
) {
    public static ExtensionPolicyResponse from(ExtensionPolicy policy) {
        return new ExtensionPolicyResponse(policy.getExtension(), policy.isBlocked());
    }
}