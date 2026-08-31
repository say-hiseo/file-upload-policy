package com.assignment.fileuploadpolicy.domain.policy.service;

import com.assignment.fileuploadpolicy.domain.policy.dto.PolicyOverview;
import com.assignment.fileuploadpolicy.domain.policy.entity.AuditAction;
import com.assignment.fileuploadpolicy.domain.policy.entity.ExtensionPolicy;
import com.assignment.fileuploadpolicy.domain.policy.entity.ExtensionPolicyAuditLog;
import com.assignment.fileuploadpolicy.domain.policy.entity.PolicyType;
import com.assignment.fileuploadpolicy.domain.policy.repository.ExtensionPolicyAuditLogRepository;
import com.assignment.fileuploadpolicy.domain.policy.repository.ExtensionPolicyRepository;
import com.assignment.fileuploadpolicy.global.auth.ActorContext;
import com.assignment.fileuploadpolicy.global.config.PolicyProperties;
import com.assignment.fileuploadpolicy.global.exception.BusinessException;
import com.assignment.fileuploadpolicy.global.exception.ErrorCode;
import java.util.List;
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

    public PolicyOverview getPolicyOverview() {
        List<ExtensionPolicy> fixed = policyRepository.findByType(PolicyType.FIXED);
        List<ExtensionPolicy> custom = policyRepository.findByType(PolicyType.CUSTOM);
        return new PolicyOverview(fixed, custom, custom.size(), policyProperties.customExtensionMaxCount());
    }

    public Page<ExtensionPolicyAuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByChangedAtDesc(pageable);
    }

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

        auditLogRepository.save(ExtensionPolicyAuditLog.record(
                extension, PolicyType.FIXED,
                blocked ? AuditAction.BLOCK : AuditAction.UNBLOCK,
                actor.memberId(), actor.username()));
    }

    @Transactional
    public ExtensionPolicy addCustom(String rawExtension, ActorContext actor) {
        String extension = normalizeOrThrow(rawExtension);

        if (extension.length() > policyProperties.customExtensionMaxLength()) {
            throw new BusinessException(ErrorCode.CUSTOM_EXTENSION_TOO_LONG,
                    policyProperties.customExtensionMaxLength());
        }

        policyRepository.findByExtension(extension).ifPresent(found -> {
            if (found.isFixed()) {
                throw new BusinessException(ErrorCode.FIXED_EXTENSION_CONFLICT, extension);
            }
            throw new BusinessException(ErrorCode.DUPLICATE_CUSTOM_EXTENSION, extension);
        });

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
}