package com.assignment.fileuploadpolicy.domain.policy;

import java.util.List;

/**
 * 정책 관리 화면(A) 전체 조회 결과. 컨트롤러가 이 record를 그대로
 * (또는 얇은 DTO로 감싸) 응답 바디로 내려준다.
 */
public record PolicyOverview(
        List<ExtensionPolicy> fixedPolicies,
        List<ExtensionPolicy> customPolicies,
        int customCount,
        int customMax
) {
}