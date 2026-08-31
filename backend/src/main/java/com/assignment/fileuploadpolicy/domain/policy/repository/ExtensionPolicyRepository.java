package com.assignment.fileuploadpolicy.domain.policy.repository;

import com.assignment.fileuploadpolicy.domain.policy.entity.ExtensionPolicy;
import com.assignment.fileuploadpolicy.domain.policy.entity.PolicyType;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExtensionPolicyRepository extends JpaRepository<ExtensionPolicy, Long> {

    Optional<ExtensionPolicy> findByExtension(String extension);

    List<ExtensionPolicy> findByType(PolicyType type);

    List<ExtensionPolicy> findByBlockedTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ExtensionPolicy p WHERE p.type = :type")
    List<ExtensionPolicy> findByTypeForUpdate(@Param("type") PolicyType type);
}