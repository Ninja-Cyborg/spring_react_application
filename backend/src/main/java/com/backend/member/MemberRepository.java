package com.backend.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface MemberRepository extends JpaRepository<Member, Integer> {
    boolean existsMemberByEmail(String email);
    boolean existsMemberById(Integer id);
    Optional<Member> findUserByEmail(String email);
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Member m SET m.profileImageId = ?1 WHERE m.id = ?2")
    int updateProfileImageId(String profileImageId, Integer id);
}
