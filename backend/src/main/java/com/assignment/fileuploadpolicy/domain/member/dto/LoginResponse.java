package com.assignment.fileuploadpolicy.domain.member.dto;

import com.assignment.fileuploadpolicy.domain.member.entity.Member;

public record LoginResponse(
        Long memberId,
        String username,
        String displayName
) {
    public static LoginResponse from(Member member) {
        return new LoginResponse(member.getId(), member.getUsername(), member.getDisplayName());
    }

    public static LoginResponse empty() {
        return new LoginResponse(null, null, null);
    }
}