package com.assignment.fileuploadpolicy.domain.member.controller;

import com.assignment.fileuploadpolicy.domain.member.dto.LoginRequest;
import com.assignment.fileuploadpolicy.domain.member.dto.LoginResponse;
import com.assignment.fileuploadpolicy.domain.member.entity.Member;
import com.assignment.fileuploadpolicy.domain.member.repository.MemberRepository;
import com.assignment.fileuploadpolicy.global.auth.SessionKeys;
import com.assignment.fileuploadpolicy.global.exception.BusinessException;
import com.assignment.fileuploadpolicy.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return Optional.ofNullable(memberId)
                .flatMap(memberRepository::findById)
                .map(LoginResponse::from)
                .orElseGet(LoginResponse::empty);
    }
}