package com.assignment.fileuploadpolicy.domain.policy;

/**
 * 확장자 정책 타입.
 * FIXED: 고정 확장자 7개 (seed, row 삭제 불가, isBlocked만 토글)
 * CUSTOM: 사용자가 등록한 커스텀 확장자 (row 존재 자체가 차단을 의미)
 */
public enum PolicyType {
    FIXED,
    CUSTOM
}