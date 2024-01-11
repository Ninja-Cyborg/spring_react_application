package com.backend.member;

import java.util.List;
import java.util.Optional;

public interface MemberDao {
    List<Member> selectAllMembers();
    Optional<Member> selectMemberById(Integer id);
    void insertMember(Member member);
    void deleteMemberById(Integer id);
    void updateMember(Member member);
    boolean existsMemberWithEmail(String email);
    boolean existsMemberWithId(Integer id);
    Optional<Member> selectUserByEmail(String email);
}
