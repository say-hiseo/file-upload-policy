package com.assignment.fileuploadpolicy.global.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

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