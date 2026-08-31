package com.assignment.fileuploadpolicy.domain.policy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 커스텀 확장자 추가 요청.
 * 20자 제한은 DB 컬럼 길이(1차 방어)와 별개로, 여기서도 Bean Validation으로
 * 한 번 더 검증해 빠르게 실패시킨다 (컨트롤러 진입 시점에 즉시 반려).
 * 실제 정규화 이후 길이 재검증은 ExtensionPolicyService에서 한 번 더 수행한다
 * (원본 입력 기준 길이와 정규화 후 길이가 다를 수 있기 때문 - 예: ".SH " -> "sh").
 */
public record AddCustomExtensionRequest(
        @NotBlank(message = "확장자를 입력해주세요")
        @Size(max = 20, message = "20자 이하로 입력해주세요")
        String extension
) {
}