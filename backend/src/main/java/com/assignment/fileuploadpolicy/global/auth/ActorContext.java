package com.assignment.fileuploadpolicy.global.auth;

/**
 * 정책 변경/업로드 요청을 수행한 주체. 더미 로그인 붙이기 전까지는
 * SYSTEM으로 채워 감사 로그가 항상 값을 갖도록 한다. (CONSIDERATIONS.md 2-2 참고)
 */
public record ActorContext(Long memberId, String username) {

    private static final String SYSTEM_USERNAME = "SYSTEM";

    public static ActorContext system() {
        return new ActorContext(null, SYSTEM_USERNAME);
    }

    public static ActorContext of(Long memberId, String username) {
        return new ActorContext(memberId, username);
    }
}