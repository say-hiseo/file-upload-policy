package com.assignment.fileuploadpolicy.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 도메인 규칙 위반 (정책 위반, 리소스 없음 등). 4xx로 매핑되며,
     * 클라이언트 입력에 대한 정상적인 거부 흐름이므로 warn 레벨로 로깅한다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("[{}] {}", e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.of(e));
    }

    /**
     * @Valid Bean Validation 실패 (예: 커스텀 확장자 20자 초과 등 DTO 레벨 제약).
     * 여러 필드 에러 중 첫 번째 메시지만 대표로 사용한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다");
        log.warn("[{}] {}", ErrorCode.INVALID_INPUT.getCode(), message);
        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
    }

    /**
     * 업로드 파일 크기 제한(1-6, application.yml의 max-file-size) 초과 시
     * Spring이 던지는 예외를 도메인 에러 형식으로 통일한다.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        String message = "파일 크기가 허용된 최대 크기를 초과했습니다";
        log.warn("[{}] {}", ErrorCode.INVALID_INPUT.getCode(), message);
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
    }

    /**
     * 예상하지 못한 모든 예외. 상세 스택트레이스는 서버 로그에만 남기고,
     * 클라이언트에는 내부 구현이 노출되지 않도록 일반화된 메시지만 전달한다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
        log.error("예상하지 못한 예외 발생", e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.formatMessage()));
    }
}