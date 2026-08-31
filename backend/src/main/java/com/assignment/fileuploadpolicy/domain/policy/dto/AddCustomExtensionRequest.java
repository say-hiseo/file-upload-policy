package com.assignment.fileuploadpolicy.domain.policy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCustomExtensionRequest(
        @NotBlank(message = "확장자를 입력해주세요")
        @Size(max = 20, message = "20자 이하로 입력해주세요")
        String extension
) {
}