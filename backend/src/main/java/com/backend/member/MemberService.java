package com.backend.member;

import com.backend.exceptions.DuplicateResourceException;
import com.backend.exceptions.RequestValidationException;
import com.backend.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final MemberDao memberDao;
    private final PasswordEncoder passwordEncoder;
    private final MemberDTOMapper memberDTOMapper;

    public MemberService(@Qualifier("jpa") MemberDao memberDao, PasswordEncoder passwordEncoder, MemberDTOMapper memberDTOMapper){
        this.memberDao = memberDao;
        this.passwordEncoder = passwordEncoder;
        this.memberDTOMapper = memberDTOMapper;
    }

    public List<MemberDTO> getAllMembers(){
        return  memberDao.selectAllMembers()
                .stream()
                .map(memberDTOMapper)
                .collect(Collectors.toList());
    }

    public MemberDTO getMember(Integer id){
        return memberDao.selectMemberById(id)
                .map(memberDTOMapper)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "member with id [%s] not found".formatted(id)
                ));
    }

    public void addMember(MemberRegistrationRequest memberRegistrationRequest){
        String email = memberRegistrationRequest.email();
        //check
        if(memberDao.existsMemberWithEmail(email)){
            throw new DuplicateResourceException("Email already taken!");
        }
        // else add
        Member newMember = new Member(
                        memberRegistrationRequest.name(),
                        memberRegistrationRequest.email(),
                        passwordEncoder.encode(memberRegistrationRequest.password()),
                        memberRegistrationRequest.age(),
                        memberRegistrationRequest.gender()
        );
        memberDao.insertMember(newMember);
    }

    public void deleteMemberById(Integer id) {
        if(!memberDao.existsMemberWithId(id)){
            throw new ResourceNotFoundException("member with id [%s] does not exist".formatted(id));
        }
        memberDao.deleteMemberById(id);
    }

    public void updateMember(Integer id, MemberUpdateRequest updateRequest) {
        Member member = memberDao.selectMemberById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "member with id [%s] not found".formatted(id)
                ));
        boolean changes = false;
        if (updateRequest.name() != null && !updateRequest.name().equals(member.getName())) {
            member.setName(updateRequest.name());
            changes = true;
        }

        if (updateRequest.age() != null && !updateRequest.age().equals(member.getAge())) {
            member.setAge(updateRequest.age());
            changes = true;
        }

        if (updateRequest.email() != null && !updateRequest.email().equals(member.getEmail())) {
            if (memberDao.existsMemberWithEmail(updateRequest.email())) {
                throw new DuplicateResourceException(
                        "email already taken"
                );
            }
            member.setEmail(updateRequest.email());
            changes = true;
        }

        if (updateRequest.gender() != null && !updateRequest.gender().equals(member.getGender())) {
            member.setGender(updateRequest.gender());
            changes = true;
        }

        if (!changes) {
            throw new RequestValidationException("no data changes found");
        }

        memberDao.updateMember(member);
    }
}