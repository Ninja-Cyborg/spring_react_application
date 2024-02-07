package com.backend.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("jpa")
public class MemberJPADataAccessService implements MemberDao {

    private final MemberRepository memberRepository;

    public MemberJPADataAccessService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public List<Member> selectAllMembers() {
        Page<Member> page = memberRepository.findAll(Pageable.ofSize(1000));
        return page.getContent();
    }

    @Override
    public Optional<Member> selectMemberById(Integer id) {
        return memberRepository.findById(id);
    }

    @Override
    public void insertMember(Member member) {
        memberRepository.save(member);
    }

    @Override
    public void deleteMemberById(Integer id) {
        memberRepository.deleteById(id);
    }

    @Override
    public void updateMember(Member member) {
        memberRepository.save(member);
    }

    @Override
    public boolean existsMemberWithEmail(String email) {
        return memberRepository.existsMemberByEmail(email);
    }

    @Override
    public boolean existsMemberWithId(Integer id) {
        return memberRepository.existsMemberById(id);
    }

    @Override
    public Optional<Member> selectUserByEmail(String email) {
        return memberRepository.findUserByEmail(email);
    }

    @Override
    public void updateMemberProfileImageId(String profileImageId, Integer id) {
        memberRepository.updateProfileImageId(profileImageId, id);
    }
}
