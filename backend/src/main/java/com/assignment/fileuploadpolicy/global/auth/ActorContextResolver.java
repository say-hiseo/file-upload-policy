package com.assignment.fileuploadpolicy.global.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 * 세션에서 현재 로그인 사용자를 조회해 ActorContext로 변환한다.
 * 로그인하지 않은 상태여도 요청 자체를 막지 않고 SYSTEM으로 대체한다
 * (더미 로그인은 감사 로그의 "누가"를 채우는 목적일 뿐, 이 과제의
 * 인가(authorization) 체계는 별도로 구현하지 않는다 - CONSIDERATIONS.md 2-2 참고).
 */
@Component
public class ActorContextResolver {

    public ActorContext resolve(HttpSession session) {
        if (session == null) {
            return ActorContext.system();
        }
        Long memberId = (Long) session.getAttribute(SessionKeys.MEMBER_ID);
        String username = (String) session.getAttribute(SessionKeys.USERNAME);
        if (memberId == null || username == null) {
            return ActorContext.system();
        }
        return ActorContext.of(memberId, username);
    }
}