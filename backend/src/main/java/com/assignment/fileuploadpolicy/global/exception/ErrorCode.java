package com.assignment.fileuploadpolicy.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ===== Policy 도메인 (A) =====
    INVALID_EXTENSION_FORMAT(HttpStatus.BAD_REQUEST, "POLICY-001",
            "올바른 확장자 형식이 아닙니다: %s"),
    DUPLICATE_CUSTOM_EXTENSION(HttpStatus.CONFLICT, "POLICY-002",
            "이미 등록된 확장자입니다: %s"),
    FIXED_EXTENSION_CONFLICT(HttpStatus.CONFLICT, "POLICY-003",
            "이미 고정 확장자 목록에 등록되어 있어 추가할 수 없습니다: %s"),
    CUSTOM_EXTENSION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "POLICY-004",
            "최대 %d개까지 등록 가능합니다"),
    EXTENSION_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "POLICY-005",
            "존재하지 않는 확장자입니다: %s"),
    FIXED_TYPE_ACTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "POLICY-006",
            "CUSTOM 타입은 체크/해제 대상이 아닙니다: %s"),
    CUSTOM_EXTENSION_TOO_LONG(HttpStatus.BAD_REQUEST, "POLICY-007",
            "%d자 이하로 입력해주세요"),
    CANNOT_REMOVE_FIXED_EXTENSION(HttpStatus.BAD_REQUEST, "POLICY-008",
            "고정 확장자는 삭제할 수 없습니다: %s"),

    // ===== Upload 도메인 (B) =====
    TOO_MANY_FILES_IN_REQUEST(HttpStatus.BAD_REQUEST, "UPLOAD-001",
            "한 번에 최대 %d개까지 업로드할 수 있습니다"),
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "UPLOAD-002",
            "빈 파일은 업로드할 수 없습니다"),
    FILENAME_TOO_LONG(HttpStatus.BAD_REQUEST, "UPLOAD-003",
            "파일명이 너무 깁니다 (최대 %d자)"),
    FILE_EXTENSION_BLOCKED(HttpStatus.BAD_REQUEST, "UPLOAD-004",
            "'%s' 확장자는 차단된 확장자입니다"),
    DANGEROUS_FILE_SIGNATURE(HttpStatus.BAD_REQUEST, "UPLOAD-005",
            "파일 내용이 실행 파일 시그니처와 일치하여 업로드가 거부되었습니다"),
    UPLOAD_LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "UPLOAD-006",
            "로그인이 필요한 기능입니다"),
    UPLOAD_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "UPLOAD-007",
            "다운로드할 수 없는 파일입니다"),
    UPLOAD_FILE_FORBIDDEN(HttpStatus.FORBIDDEN, "UPLOAD-008",
            "본인이 업로드한 파일만 다운로드할 수 있습니다"),
    FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "UPLOAD-009",
            "파일 크기가 허용된 최대 크기를 초과했습니다"),


    // ===== Auth 도메인 =====
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-001",
            "아이디 또는 비밀번호가 올바르지 않습니다"),

    // ===== 공통 =====
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON-001", "잘못된 요청입니다: %s"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-999",
            "서버 내부 오류가 발생했습니다");

    private final HttpStatus status;
    private final String code;
    private final String messageTemplate;

    ErrorCode(HttpStatus status, String code, String messageTemplate) {
        this.status = status;
        this.code = code;
        this.messageTemplate = messageTemplate;
    }

    public String formatMessage(Object... args) {
        if (args == null || args.length == 0) {
            return messageTemplate;
        }
        return String.format(messageTemplate, args);
    }
}