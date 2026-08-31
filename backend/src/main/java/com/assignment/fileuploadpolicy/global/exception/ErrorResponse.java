package com.assignment.fileuploadpolicy.global.exception;

import java.time.OffsetDateTime;

/**
 * 모든 API 에러 응답의 공통 형태.
 * 요구사항 3-1 "무엇이 왜 막혔는지"를 code(왜: 어떤 규칙 위반인지)와
 * message(무엇이: 구체적 대상 포함된 문장)로 나눠서 전달한다.
 */
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