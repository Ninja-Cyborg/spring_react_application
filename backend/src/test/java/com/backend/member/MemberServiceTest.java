package com.backend.member;

import com.backend.exceptions.DuplicateResourceException;
import com.backend.exceptions.RequestValidationException;
import com.backend.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberDao memberDao;
    @Mock
    private PasswordEncoder passwordEncoder;
    private MemberService underTest;
    private final MemberDTOMapper memberDTOMapper = new MemberDTOMapper();

    @BeforeEach
    void setUp() {
        underTest = new MemberService(memberDao, passwordEncoder, memberDTOMapper);
    }

    @Test
    void getAllMembers() {
        underTest.getAllMembers();

        verify(memberDao).selectAllMembers();
    }

    @Test
    void getMember() {
        int id = 7;
        Member member = new Member(
                id, "Jay","Jay@email.co", "password", 25, Gender.MALE
                );

        when(memberDao.selectMemberById(id))
                .thenReturn(Optional.of(member));

        MemberDTO expected = memberDTOMapper.apply(member);

        MemberDTO actual = underTest.getMember(7);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void willThrowExceptionWhenGetMemberIsEmpty(){
        int id = 7;

        when(memberDao.selectMemberById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.getMember(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("member with id [%s] not found".formatted(id));
    }

    @Test
    void addMember() {
        String email = "jo@mail.corp";
        when(memberDao.existsMemberWithEmail(email))
                .thenReturn(false);

        MemberRegistrationRequest request = new MemberRegistrationRequest(
                "Jo", 18, email, "password", Gender.MALE
        );

        String passwordHash = "$2132546154cvav";

        when(passwordEncoder.encode(request.password())).thenReturn(passwordHash);

        underTest.addMember(request);
        ArgumentCaptor<Member> memberArgumentCaptor = ArgumentCaptor.forClass(
                Member.class
        );

        verify(memberDao).insertMember(memberArgumentCaptor.capture());

        Member capturedMember = memberArgumentCaptor.getValue();

        assertThat(capturedMember.getId()).isNull();
        assertThat(capturedMember.getName()).isEqualTo(request.name());
        assertThat(capturedMember.getEmail()).isEqualTo(request.email());
        assertThat(capturedMember.getPassword()).isEqualTo(passwordHash);
        assertThat(capturedMember.getAge()).isEqualTo(request.age());
    }

    @Test
    void addMemberThrowExceptionWhenEmailExist(){
        String email = "jo@mail.corp";
        when(memberDao.existsMemberWithEmail(email)).thenReturn(true);
        String password = passwordEncoder.encode("password");

        MemberRegistrationRequest request = new MemberRegistrationRequest(
                "Jo", 18, email, password, Gender.MALE
        );

        // adding member throw exception
        assertThatThrownBy(() -> underTest.addMember(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already taken!");

        // verify no member is inserted
        verify(memberDao, never()).insertMember(any());
    }

    @Test
    void deleteMemberById() {
        int id = 10;

        when(memberDao.existsMemberWithId(id)).thenReturn(true);

        underTest.deleteMemberById(id);

        verify(memberDao).deleteMemberById(id);
    }

    @Test
    void updateMember() {
        int id = 8;
        String email = "jo@mail.corp";
        Gender gender = Gender.MALE;
        Member member = new Member(id, "Jo", email, "password", 18, gender);

        when(memberDao.selectMemberById(id)).thenReturn(Optional.of(member));

        // update member
        String updateEmail = email + ".co";
        MemberUpdateRequest updateRequest = new MemberUpdateRequest("Joe", updateEmail, 19, gender);
        when(memberDao.existsMemberWithEmail(updateEmail)).thenReturn(false);

        underTest.updateMember(id, updateRequest);

        ArgumentCaptor<Member> memberArgumentCaptor = ArgumentCaptor.forClass(Member.class);

        verify(memberDao).updateMember(memberArgumentCaptor.capture());

        Member capturedMember = memberArgumentCaptor.getValue();

        assertThat(capturedMember.getName()).isEqualTo(updateRequest.name());
        assertThat(capturedMember.getEmail()).isEqualTo(updateRequest.email());
        assertThat(capturedMember.getAge()).isEqualTo(updateRequest.age());

    }

    @Test
    void doNotUpdateMemberWhenNoChangesPassed(){
        int id = 12;
        Gender gender = Gender.MALE;
        Member member = new Member(id, "Jo", "jo@mail.corp", "password", 18, gender);
        when(memberDao.selectMemberById(id)).thenReturn(Optional.of(member));

        // pass update request with same values
        MemberUpdateRequest updateRequest = new MemberUpdateRequest(member.getName(),
                                                                    member.getEmail(),
                                                                    member.getAge(),
                                                                    member.getGender());

        assertThatThrownBy(() -> underTest.updateMember(id, updateRequest))
                .isInstanceOf(RequestValidationException.class).hasMessage("no data changes found");

        // verify members do not change
        verify(memberDao, never()).updateMember(any());
    }
}