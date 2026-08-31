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
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 확장자 차단 정책 (고정 + 커스텀 통합 관리).
 * extension 컬럼 전역 unique 제약으로 고정/커스텀 겹침을 구조적으로 방지한다.
 * (CONSIDERATIONS.md 2-1 참고)
 */
@Entity
@Table(name = "extension_policy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ExtensionPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 정규화된 값만 저장한다는 전제. 정규화 자체는 ExtensionNormalizer가 담당하고,
    // 여기서는 이미 정규화된 문자열만 받는다.
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

    /**
     * FIXED 확장자 seed 생성 전용. 애플리케이션 기동 후 사용자가 이 메서드로
     * 새 FIXED row를 추가하는 경로는 없다 (요구사항상 고정 목록은 불변).
     * 기본 unchecked(false) 상태로 시작한다.
     */
    public static ExtensionPolicy createFixed(String normalizedExtension) {
        return new ExtensionPolicy(normalizedExtension, PolicyType.FIXED, false);
    }

    /**
     * 커스텀 확장자 등록. CUSTOM은 "row 존재 = 차단"이 원칙이므로 항상 blocked=true로 시작한다.
     */
    public static ExtensionPolicy createCustom(String normalizedExtension) {
        return new ExtensionPolicy(normalizedExtension, PolicyType.CUSTOM, true);
    }

    /**
     * FIXED 타입만 체크/해제가 가능하다. CUSTOM에 호출하면 정책 위반이므로 예외를 던진다.
     */
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
            throw new com.assignment.fileuploadpolicy.global.exception.BusinessException(
                    com.assignment.fileuploadpolicy.global.exception.ErrorCode.FIXED_TYPE_ACTION_NOT_ALLOWED,
                    extension);
        }
    }

    public boolean isFixed() {
        return this.type == PolicyType.FIXED;
    }

    public boolean isCustom() {
        return this.type == PolicyType.CUSTOM;
    }
}