package com.assignment.fileuploadpolicy.domain.policy.entity;

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