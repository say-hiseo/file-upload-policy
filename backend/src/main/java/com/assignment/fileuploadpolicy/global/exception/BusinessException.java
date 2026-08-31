package com.assignment.fileuploadpolicy.global.exception;

import lombok.Getter;

/**
 * 모든 도메인 예외의 공통 부모.
 * ErrorCode 하나로 HTTP 상태코드, 에러코드, 메시지 템플릿을 함께 들고 다니므로
 * 도메인별로 별도 예외 클래스를 만들 필요가 없다.
 *
 * 사용 예:
 *   throw new BusinessException(ErrorCode.DUPLICATE_CUSTOM_EXTENSION, extension);
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(errorCode.formatMessage(args));
        this.errorCode = errorCode;
    }
}