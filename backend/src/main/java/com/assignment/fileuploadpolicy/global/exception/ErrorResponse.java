package com.assignment.fileuploadpolicy.global.exception;

import java.time.OffsetDateTime;

public record ErrorResponse(
        String code,
        String message,
        OffsetDateTime timestamp
) {
    public static ErrorResponse of(BusinessException e) {
        return new ErrorResponse(e.getErrorCode().getCode(), e.getMessage(), OffsetDateTime.now());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getCode(), message, OffsetDateTime.now());
    }
}