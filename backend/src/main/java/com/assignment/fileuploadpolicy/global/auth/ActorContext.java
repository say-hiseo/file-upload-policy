package com.assignment.fileuploadpolicy.global.auth;

public record ActorContext(Long memberId, String username) {

    private static final String SYSTEM_USERNAME = "SYSTEM";

    public static ActorContext system() {
        return new ActorContext(null, SYSTEM_USERNAME);
    }

    public static ActorContext of(Long memberId, String username) {
        return new ActorContext(memberId, username);
    }
}