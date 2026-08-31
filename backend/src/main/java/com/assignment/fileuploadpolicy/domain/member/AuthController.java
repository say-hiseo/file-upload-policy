package com.assignment.fileuploadpolicy.domain.member;

import com.assignment.fileuploadpolicy.domain.member.dto.LoginRequest;
import com.assignment.fileuploadpolicy.domain.member.dto.LoginResponse;
import com.assignment.fileuploadpolicy.global.auth.SessionKeys;
import com.assignment.fileuploadpolicy.global.exception.BusinessException;
import com.assignment.fileuploadpolicy.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 더미 로그인. 실제 인증(비밀번호 해싱, 토큰 검증)은 하지 않으며,
 * 정책 변경 이력에 "누가"를 남기기 위한 최소한의 식별 수단이다.
 * (CONSIDERATIONS.md 2-2 참고)
 */
@Tag(name = "Auth", description = "더미 로그인 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberRepository memberRepository;

    @Operation(summary = "로그인", description = "더미 계정으로 로그인. 세션에 사용자 정보를 저장한다")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        Member member = memberRepository.findByUsername(request.username())
                .filter(m -> m.matchesPassword(request.password()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        session.setAttribute(SessionKeys.MEMBER_ID, member.getId());
        session.setAttribute(SessionKeys.USERNAME, member.getUsername());

        return LoginResponse.from(member);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @Operation(summary = "현재 로그인 사용자 조회", description = "로그인하지 않았으면 모든 필드가 null로 내려온다")
    @GetMapping("/me")
    public LoginResponse me(HttpSession session) {
        Long memberId = (Long) session.getAttribute(SessionKeys.MEMBER_ID);
        String username = (String) session.getAttribute(SessionKeys.USERNAME);
        if (memberId == null) {
            return new LoginResponse(null, null, null);
        }
        return memberRepository.findById(memberId)
                .map(LoginResponse::from)
                .orElse(new LoginResponse(null, null, null));
    }
}