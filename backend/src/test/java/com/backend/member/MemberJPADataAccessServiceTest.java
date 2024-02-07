package com.backend.member;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class MemberJPADataAccessServiceTest {

    private MemberJPADataAccessService underTest;
    private AutoCloseable autoCloseable;
    private Random random;

    @Mock
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new MemberJPADataAccessService(memberRepository);
    }

    @AfterEach
    void tearDown() throws Exception{
        autoCloseable.close();
    }

    @Test
    void selectAllMembers() {
        // crud + pagination

        Page<Member> page = mock(Page.class);
        List<Member> members = List.of(new Member());

        when(page.getContent()).thenReturn(members);
        when(memberRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<Member> expectedList = underTest.selectAllMembers();

        assertThat(expectedList).isEqualTo(members);
        ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(memberRepository).findAll(pageableArgumentCaptor.capture());
        assertThat(pageableArgumentCaptor.getValue()).isEqualTo(Pageable.ofSize(1000));
    }

    @Test
    void selectMemberById() {
        int id = 1;

        underTest.selectMemberById(id);

        verify(memberRepository).findById(id);
    }

    @Test
    void insertMember() {
        random = new Random();
        boolean check = random.nextBoolean();
        int age = random.nextInt(16, 75);
        String password = "password";
        Gender gender = age % 2 == 0 ? Gender.MALE : Gender.FEMALE;

        Member member = new Member(
                1,
                "Jake Kai",
                "J.Kai@mail.corp",
                password,
                22,
                gender
        );

        underTest.insertMember(member);

        verify(memberRepository).save(member);
    }

    @Test
    void deleteMemberById() {
        int id = 1;

        underTest.deleteMemberById(id);

        verify(memberRepository).deleteById(id);
    }

    @Test
    void updateMember() {
        Member member = new Member(
                1,
                "Jake Kai",
                "J.Kai@mail.corp",
                "password",
                22,
                Gender.MALE
        );

        underTest.updateMember(member);

        verify(memberRepository).save(member);
    }

    @Test
    void existsMemberWithEmail() {
        String email = "random.mail@red.corp";

        underTest.existsMemberWithEmail(email);

        verify(memberRepository).existsMemberByEmail(email);
    }

    @Test
    void existsMemberWithId() {
        int id = 1;

        underTest.existsMemberWithId(id);

        verify(memberRepository).existsMemberById(id);
    }

    @Test
    void canUpdateMemberProfileImageId(){
        // Given
        String profileImageId = "3241";
        int id = 1;

        // When
        underTest.updateMemberProfileImageId(profileImageId, id);

        // Then

        verify(memberRepository).updateProfileImageId(profileImageId, id);
    }
}