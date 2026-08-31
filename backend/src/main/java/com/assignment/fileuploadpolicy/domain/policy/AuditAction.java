package com.assignment.fileuploadpolicy.domain.policy;

/**
 * 정책 변경 이력의 액션 종류.
 * BLOCK/UNBLOCK: FIXED 확장자 체크/해제
 * ADD/REMOVE: CUSTOM 확장자 등록/삭제
 */
public enum AuditAction {
    BLOCK,
    UNBLOCK,
    ADD,
    REMOVE
}