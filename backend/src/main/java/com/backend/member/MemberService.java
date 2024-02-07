package com.backend.member;

import com.backend.exceptions.DuplicateResourceException;
import com.backend.exceptions.RequestValidationException;
import com.backend.exceptions.ResourceNotFoundException;
import com.backend.s3.S3Buckets;
import com.backend.s3.S3Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final MemberDao memberDao;
    private final PasswordEncoder passwordEncoder;
    private final MemberDTOMapper memberDTOMapper;
    private final S3Service s3Service;
    private final S3Buckets s3Buckets;

    public MemberService(@Qualifier("jpa") MemberDao memberDao, PasswordEncoder passwordEncoder, MemberDTOMapper memberDTOMapper, S3Service s3Service, S3Buckets s3Buckets){
        this.memberDao = memberDao;
        this.passwordEncoder = passwordEncoder;
        this.memberDTOMapper = memberDTOMapper;
        this.s3Service = s3Service;
        this.s3Buckets = s3Buckets;
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
        checkIfMemberExistsOrThrow(id);
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

    public void uploadMemberProfileImage(Integer id, MultipartFile file) {
        checkIfMemberExistsOrThrow(id);
        String profileImageId = UUID.randomUUID().toString();
        try {
            s3Service.putObject(
                    s3Buckets.getMember(),
                    "profile-image/%s/%s".formatted(id, profileImageId),
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new RuntimeException("failed to upload profile image", e);
        }
        memberDao.updateMemberProfileImageId(profileImageId, id);
    }

    public byte[] getMemberProfileImage(Integer id) {
        var member = memberDao.selectMemberById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "member with id [%s] not found".formatted(id)
                ));

        if(StringUtils.isBlank(member.getProfileImageId())){
            throw new ResourceNotFoundException(
                    "No Profile Image exits for member with id [%s]".formatted(id)
            );
        }
       
        byte[] profileImage = s3Service.getObject(
                s3Buckets.getMember(),
                "profile-image/%s/%s".formatted(id, member.getProfileImageId())
        );
        return profileImage;
    }

    private void checkIfMemberExistsOrThrow(Integer id) {
        if(!memberDao.existsMemberWithId(id)){
            throw new ResourceNotFoundException("member with id [%s] does not exist".formatted(id));
        }
    }
}