package com.backend.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

//@Transactional
public interface MemberRepository extends JpaRepository<Member, Integer> {
    boolean existsMemberByEmail(String email);
    boolean existsMemberById(Integer id);
    Optional<Member> findUserByEmail(String email);
}
