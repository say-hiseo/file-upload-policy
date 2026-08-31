package com.assignment.fileuploadpolicy.domain.policy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * 정책 변경 이력 (불변 로그).
 * member를 @ManyToOne으로 객체 매핑하지 않고 memberId(FK 컬럼) + username(스냅샷)으로
 * 분리한다. member가 삭제되어도(ON DELETE SET NULL) 이 로그는 "당시 누구였는지"를
 * 텍스트로 계속 보존한다. (CONSIDERATIONS.md 4장 FK 설계 판단 참고)
 * extension/type 역시 extension_policy에 대한 FK가 아닌 스냅샷이다 — CUSTOM 확장자는
 * 삭제 시 정책 테이블에서 row 자체가 사라지는 게 정상 흐름이라, FK를 걸면
 * "정상적인 삭제"와 "감사 로그 영구 보존"이 충돌하기 때문이다.
 */
@Entity
@Table(name = "extension_policy_audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ExtensionPolicyAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String extension;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PolicyType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuditAction action;

    @Column(name = "changed_by_member_id")
    private Long changedByMemberId;

    @Column(name = "changed_by_username", nullable = false, length = 50)
    private String changedByUsername;

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private OffsetDateTime changedAt;

    private ExtensionPolicyAuditLog(String extension, PolicyType type, AuditAction action,
                                     Long changedByMemberId, String changedByUsername) {
        this.extension = extension;
        this.type = type;
        this.action = action;
        this.changedByMemberId = changedByMemberId;
        this.changedByUsername = changedByUsername;
    }

    public static ExtensionPolicyAuditLog record(String extension, PolicyType type, AuditAction action,
                                                  Long changedByMemberId, String changedByUsername) {
        return new ExtensionPolicyAuditLog(extension, type, action, changedByMemberId, changedByUsername);
    }
}