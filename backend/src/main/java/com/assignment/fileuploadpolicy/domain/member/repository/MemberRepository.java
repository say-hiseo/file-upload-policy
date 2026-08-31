package com.assignment.fileuploadpolicy.domain.member.repository;

import com.assignment.fileuploadpolicy.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUsername(String username);
}