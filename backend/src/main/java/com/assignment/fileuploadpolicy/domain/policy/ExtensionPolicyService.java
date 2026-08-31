package com.assignment.fileuploadpolicy.domain.policy;

import com.assignment.fileuploadpolicy.global.auth.ActorContext;
import com.assignment.fileuploadpolicy.global.config.PolicyProperties;
import com.assignment.fileuploadpolicy.global.exception.BusinessException;
import com.assignment.fileuploadpolicy.global.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExtensionPolicyService {

    private final ExtensionPolicyRepository policyRepository;
    private final ExtensionPolicyAuditLogRepository auditLogRepository;
    private final PolicyProperties policyProperties;

    /**
     * 정책 화면(A) 조회. FIXED/CUSTOM을 분리해서 반환하며,
     * 이 메서드가 반환하는 값이 곧 화면에 그려질 상태의 유일한 근거다.
     */
    public PolicyOverview getPolicyOverview() {
        List<ExtensionPolicy> fixed = policyRepository.findByType(PolicyType.FIXED);
        List<ExtensionPolicy> custom = policyRepository.findByType(PolicyType.CUSTOM);
        return new PolicyOverview(fixed, custom, custom.size(), policyProperties.customExtensionMaxCount());
    }

    /**
     * 고정 확장자 체크/해제. rawExtension은 화면에서 이미 정해진 값(체크박스 라벨)이
     * 넘어오므로 정규화는 방어적으로만 한 번 더 거친다.
     */
    @Transactional
    public void toggleFixed(String rawExtension, boolean blocked, ActorContext actor) {
        String extension = normalizeOrThrow(rawExtension);

        ExtensionPolicy policy = policyRepository.findByExtension(extension)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXTENSION_POLICY_NOT_FOUND, extension));

        if (!policy.isFixed()) {
            throw new BusinessException(ErrorCode.FIXED_TYPE_ACTION_NOT_ALLOWED, extension);
        }

        if (blocked) {
            policy.block();
        } else {
            policy.unblock();
        }
        // 변경 감지(dirty checking)로 UPDATE는 자동 반영됨 - 별도 save() 호출 불필요

        auditLogRepository.save(ExtensionPolicyAuditLog.record(
                extension, PolicyType.FIXED,
                blocked ? AuditAction.BLOCK : AuditAction.UNBLOCK,
                actor.memberId(), actor.username()));
    }

    /**
     * 커스텀 확장자 등록. 검증 순서는 요구사항 문서 A-2 흐름을 그대로 따른다:
     * 형식 -> 길이 -> 전역 중복(고정/커스텀) -> 개수 제한(락) 순.
     */
    @Transactional
    public ExtensionPolicy addCustom(String rawExtension, ActorContext actor) {
        String extension = normalizeOrThrow(rawExtension);

        if (extension.length() > policyProperties.customExtensionMaxLength()) {
            throw new BusinessException(ErrorCode.CUSTOM_EXTENSION_TOO_LONG,
                    policyProperties.customExtensionMaxLength());
        }

        Optional<ExtensionPolicy> existing = policyRepository.findByExtension(extension);
        if (existing.isPresent()) {
            ExtensionPolicy found = existing.get();
            if (found.isFixed()) {
                throw new BusinessException(ErrorCode.FIXED_EXTENSION_CONFLICT, extension);
            }
            throw new BusinessException(ErrorCode.DUPLICATE_CUSTOM_EXTENSION, extension);
        }

        // 동시성 방어: 락을 건 상태에서 개수를 다시 센다 (TOCTOU 방지)
        List<ExtensionPolicy> lockedCustomPolicies = policyRepository.findByTypeForUpdate(PolicyType.CUSTOM);
        if (lockedCustomPolicies.size() >= policyProperties.customExtensionMaxCount()) {
            throw new BusinessException(ErrorCode.CUSTOM_EXTENSION_LIMIT_EXCEEDED,
                    policyProperties.customExtensionMaxCount());
        }

        ExtensionPolicy saved = policyRepository.save(ExtensionPolicy.createCustom(extension));

        auditLogRepository.save(ExtensionPolicyAuditLog.record(
                extension, PolicyType.CUSTOM, AuditAction.ADD,
                actor.memberId(), actor.username()));

        return saved;
    }

    /**
     * 커스텀 확장자 삭제 (X 클릭). FIXED 확장자가 잘못 전달되는 걸 방어적으로 차단한다.
     */
    @Transactional
    public void removeCustom(String rawExtension, ActorContext actor) {
        String extension = normalizeOrThrow(rawExtension);

        ExtensionPolicy policy = policyRepository.findByExtension(extension)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXTENSION_POLICY_NOT_FOUND, extension));

        if (policy.isFixed()) {
            throw new BusinessException(ErrorCode.CANNOT_REMOVE_FIXED_EXTENSION, extension);
        }

        policyRepository.delete(policy);

        auditLogRepository.save(ExtensionPolicyAuditLog.record(
                extension, PolicyType.CUSTOM, AuditAction.REMOVE,
                actor.memberId(), actor.username()));
    }

    private String normalizeOrThrow(String rawExtension) {
        return ExtensionNormalizer.normalize(rawExtension)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_EXTENSION_FORMAT, rawExtension));
    }

    public Page<ExtensionPolicyAuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByChangedAtDesc(pageable);
    }
}