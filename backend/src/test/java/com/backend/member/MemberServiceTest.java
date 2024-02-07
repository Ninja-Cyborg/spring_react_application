package com.backend.member;

import com.backend.exceptions.DuplicateResourceException;
import com.backend.exceptions.RequestValidationException;
import com.backend.exceptions.ResourceNotFoundException;
import com.backend.s3.S3Buckets;
import com.backend.s3.S3Service;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.verification.NoMoreInteractions;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @Mock
    private S3Buckets s3Buckets;
    @Mock
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        underTest = new MemberService(
                memberDao,
                passwordEncoder,
                memberDTOMapper,
                s3Service,
                s3Buckets);
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

        when(memberDao.selectMemberById(id)).thenReturn(Optional.of(member));

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
    void willThrowDeleteMemberByIdIfNotExist() {
        int id = 10;

        when(memberDao.existsMemberWithId(id)).thenReturn(false);

        assertThatThrownBy(() -> underTest.deleteMemberById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("member with id [%s] does not exist".formatted(id));

        verify(memberDao, never()).deleteMemberById(id);
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

    @Test
    public void canUploadProfileImage() throws IOException {
        // Given
        int id = 12;

        when(memberDao.existsMemberWithId(id)).thenReturn(true);

        byte[] fileDataInBytes = "Test file".getBytes();
        MultipartFile multipartFile = new MockMultipartFile("file", fileDataInBytes);

        String bucket = "member-bucket";
        when(s3Buckets.getMember()).thenReturn(bucket);

        // when
        underTest.uploadMemberProfileImage(id, multipartFile);

        // then
        // capture profileImageId
        ArgumentCaptor<String> profileImageIdArgumentCaptor = ArgumentCaptor.forClass(String.class);

        verify(memberDao).updateMemberProfileImageId(
                profileImageIdArgumentCaptor.capture(),
                eq(id)
        );

        // MemberService file naming convention: "profile-image/%s/%s".formatted(id, profileImageId)
        String fileKeyCaptured = "profile-image/%s/%s".formatted(id, profileImageIdArgumentCaptor.getValue());

        verify(s3Service).putObject(bucket, fileKeyCaptured, fileDataInBytes);
    }

    @Test
    void cannotUploadProfileImageWhenMemberDoesNotExists(){
        // Given
        int id = 19;

        when(memberDao.existsMemberWithId(id)).thenReturn(false);

        // When
        assertThatThrownBy(() -> underTest.uploadMemberProfileImage(
                id, mock(MultipartFile.class))
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("member with id [%s] does not exist".formatted(id));

        // Then
        verify(memberDao).existsMemberWithId(id);
        verifyNoMoreInteractions(memberDao);
        verifyNoInteractions(s3Buckets);
        verifyNoInteractions(s3Service);
    }

    @Test
    void cannotUploadProfileImageWhenExceptionIsThrown() throws IOException{
        // Given
        int id = 19;

        when(memberDao.existsMemberWithId(id)).thenReturn(true);

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getBytes()).thenThrow(IOException.class);

        String bucket = "member-bucket";
        when(s3Buckets.getMember()).thenReturn(bucket);

        // When
        assertThatThrownBy(() -> {
            underTest.uploadMemberProfileImage(id, multipartFile);
        }).isInstanceOf(RuntimeException.class)
                .hasMessage("failed to upload profile image")
                .hasRootCauseInstanceOf(IOException.class);

        // Then
        verify(memberDao, never()).updateMemberProfileImageId(any(), any());
    }

    @Test
    void canDownloadProfileImage() {
        // Given
        int id = 19;
        String profileImageId = "24132";
        Member member = new Member(
               id,
                "",
                "ninja00@ninja.go",
                "password",
                19,
                Gender.FEMALE,
                profileImageId
        );

        when(memberDao.selectMemberById(id)).thenReturn(Optional.of(member));

        String bucket = "member-bucket";
        when(s3Buckets.getMember()).thenReturn(bucket);

        byte[] expectedFile = "Profile-Image".getBytes();

        String fileKey = "profile-image/%s/%s".formatted(id, profileImageId);

        when(s3Service.getObject(
                bucket,
                fileKey)
        ).thenReturn(expectedFile);

        // When
        byte[] actualFile = underTest.getMemberProfileImage(id);

        // Then
        assertThat(actualFile).isEqualTo(expectedFile);
    }

    @Test
    void cannotDownloadWhenNoProfileImageId(){
        // Given
        int id = 19;
        Member member = new Member(
                id,
                "",
                "ninja00@ninja.go",
                "password",
                19,
                Gender.FEMALE
        );

        when(memberDao.selectMemberById(id)).thenReturn(Optional.of(member));

        // WHEN
        // THEN
        assertThatThrownBy(() -> underTest.getMemberProfileImage(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No Profile Image exits for member with id [%s]".formatted(id));

        verifyNoInteractions(s3Buckets);
        verifyNoInteractions(s3Service);
    }

    @Test
    void cannotDownloadProfileImageWhenMemberDoesNotExists() {
        // Given
        int id = 19;

        when(memberDao.selectMemberById(id)).thenReturn(Optional.empty());

        // When
        // Then
        assertThatThrownBy(() -> underTest.getMemberProfileImage(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("member with id [%s] not found".formatted(id));

        verifyNoInteractions(s3Buckets);
        verifyNoInteractions(s3Service);
    }
}