package com.assignment.fileuploadpolicy.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 더미 로그인용 사용자.
 * 실제 인증(비밀번호 해싱, 세션/토큰 검증)은 과제 범위를 벗어난다고 판단해
 * 최소한의 식별자 역할만 담당한다. (CONSIDERATIONS.md 2-2 참고)
 */
@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // 평문 저장 (데모 목적). 실서비스 전환 시 BCrypt 등 해싱 필수.
    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    private Member(String username, String password, String displayName) {
        this.username = username;
        this.password = password;
        this.displayName = displayName;
    }

    public static Member create(String username, String password, String displayName) {
        return new Member(username, password, displayName);
    }

    /**
     * 더미 로그인 검증. 실제 인증이 아니므로 평문 비교로 충분하다고 판단.
     */
    public boolean matchesPassword(String rawPassword) {
        return this.password.equals(rawPassword);
    }
}