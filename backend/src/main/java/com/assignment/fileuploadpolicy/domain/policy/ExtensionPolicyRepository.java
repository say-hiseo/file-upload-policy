package com.assignment.fileuploadpolicy.domain.policy;

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

    long countByType(PolicyType type);

    /**
     * 업로드 검증(B)이 참조하는 "현재 유효한 차단 목록"의 유일한 조회 지점.
     * 정책 화면(A)의 판단 기준과 반드시 동일한 결과를 반환해야 한다.
     * (요구사항 문서 "A와 B가 같은 쿼리를 공유해야 하는 이유" 참고)
     */
    List<ExtensionPolicy> findByBlockedTrue();
    /**
     * CUSTOM 확장자 등록 시 200개 제한을 동시성 안전하게 검사하기 위한 락 조회.
     * PESSIMISTIC_WRITE는 PostgreSQL에서 "SELECT ... FOR UPDATE"로 변환된다.
     * 동시에 두 트랜잭션이 이 쿼리를 호출하면, 뒤 트랜잭션은 앞 트랜잭션이
     * 커밋(또는 롤백)할 때까지 블로킹되었다가 최신 커밋된 상태를 보고 재개된다.
     *
     * 한계: CUSTOM row가 0개인 시점(테이블이 비어있을 때)에는 잠글 대상
     * row 자체가 없어 이 락이 효과를 내지 못한다. 다만 이 시나리오는
     * 200개 한도 근처의 실질적 위험 구간이 아니라, 극히 드문 엣지 케이스로
     * 판단해 별도 대응(예: advisory lock)은 하지 않았다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ExtensionPolicy p WHERE p.type = :type")
    List<ExtensionPolicy> findByTypeForUpdate(@Param("type") PolicyType type);
}