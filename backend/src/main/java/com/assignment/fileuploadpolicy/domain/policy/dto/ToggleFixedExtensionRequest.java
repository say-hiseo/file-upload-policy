package com.assignment.fileuploadpolicy.domain.policy.dto;

import jakarta.validation.constraints.NotNull;

public record ToggleFixedExtensionRequest(
        @NotNull(message = "blocked 값은 필수입니다")
        Boolean blocked
) {
}