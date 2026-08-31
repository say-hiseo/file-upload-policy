package com.assignment.fileuploadpolicy.domain.policy.entity;

import com.assignment.fileuploadpolicy.global.exception.BusinessException;
import com.assignment.fileuploadpolicy.global.exception.ErrorCode;
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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "extension_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ExtensionPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String extension;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PolicyType type;

    @Column(name = "is_blocked", nullable = false)
    private boolean blocked;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    private ExtensionPolicy(String extension, PolicyType type, boolean blocked) {
        this.extension = extension;
        this.type = type;
        this.blocked = blocked;
    }

    public static ExtensionPolicy createFixed(String normalizedExtension) {
        return new ExtensionPolicy(normalizedExtension, PolicyType.FIXED, false);
    }

    public static ExtensionPolicy createCustom(String normalizedExtension) {
        return new ExtensionPolicy(normalizedExtension, PolicyType.CUSTOM, true);
    }

    public void block() {
        requireFixedType();
        this.blocked = true;
    }

    public void unblock() {
        requireFixedType();
        this.blocked = false;
    }

    private void requireFixedType() {
        if (this.type != PolicyType.FIXED) {
            throw new BusinessException(ErrorCode.FIXED_TYPE_ACTION_NOT_ALLOWED, extension);
        }
    }

    public boolean isFixed() {
        return this.type == PolicyType.FIXED;
    }

    public boolean isCustom() {
        return this.type == PolicyType.CUSTOM;
    }
}