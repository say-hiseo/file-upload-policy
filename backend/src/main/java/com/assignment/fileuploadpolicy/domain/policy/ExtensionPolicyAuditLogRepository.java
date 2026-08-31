package com.assignment.fileuploadpolicy.domain.policy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExtensionPolicyAuditLogRepository extends JpaRepository<ExtensionPolicyAuditLog, Long> {

    Page<ExtensionPolicyAuditLog> findAllByOrderByChangedAtDesc(Pageable pageable);
}