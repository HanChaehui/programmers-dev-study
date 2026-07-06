package com.example.spring.board1signup.domain.repository;

import com.example.spring.board1signup.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUserId(String userId);
}
