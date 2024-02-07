package com.backend.member;

import com.backend.AbstractTestContainer;
import com.backend.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestConfig.class})
class MemberRepositoryTest extends AbstractTestContainer {

    @Autowired
    private MemberRepository repoUnderTest;

    @BeforeEach
    void setUp() {
    }

    @Test
    void existsMemberByEmail() {
        String email = FAKER.internet().safeEmailAddress();
        Member member = new Member(
                FAKER.name().fullName(),
                email,
                "password", FAKER.number().numberBetween(16,75),
                Gender.NA);
        repoUnderTest.save(member);

        int id = repoUnderTest.findAll()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Member::getId)
                .findFirst()
                .orElseThrow();

        var actual = repoUnderTest.existsMemberByEmail(email);

        assertThat(actual).isTrue();
    }

    @Test
    void memberNotExistsWithoutEmail() {
        String email = FAKER.internet().safeEmailAddress();

        var actual = repoUnderTest.existsMemberByEmail(email);

        assertThat(actual).isFalse();
    }

    @Test
    void existsMemberById() {
        String email = FAKER.internet().safeEmailAddress();
        Member member = new Member(
                FAKER.name().fullName(),
                email,
                "password", FAKER.number().numberBetween(16,75),
                Gender.NA);
        repoUnderTest.save(member);

        int id = repoUnderTest.findAll()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Member::getId)
                .findFirst()
                .orElseThrow();

        var actual = repoUnderTest.existsMemberById(id);

        assertThat(actual).isTrue();
    }

    @Test
    void memberNotExistWithInvalidId(){
        int id = -1;

        var actual = repoUnderTest.existsMemberById(id);

        assertThat(actual).isFalse();
    }

    @Test
    void canUpdateProfileImageId(){
        // Given
        String email = FAKER.internet().safeEmailAddress();
        Member member = new Member(
                FAKER.name().fullName(),
                email,
                "password", FAKER.number().numberBetween(16,75),
                Gender.NA);
        repoUnderTest.save(member);

        int id = repoUnderTest.findAll()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Member::getId)
                .findFirst()
                .orElseThrow();

        // When
        repoUnderTest.updateProfileImageId( "2145", id);

        // Then
        Optional<Member> memberOptional = repoUnderTest.findById(id);

        assertThat(memberOptional).isPresent().hasValueSatisfying(
                m -> assertThat(m.getProfileImageId()).isEqualTo("2145")
        );
    }
}