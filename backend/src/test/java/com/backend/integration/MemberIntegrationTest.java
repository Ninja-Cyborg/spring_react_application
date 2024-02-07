package com.backend.integration;

import com.backend.member.*;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.shaded.com.google.common.io.Files;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

// test API/CRUD flow
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class MemberIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;
    private static final String MEMBER_URI = "/api/v1/members";
    private static final Random RANDOM = new Random();

    @Test
    void canRegisterMember(){
        // create registration request
        Faker faker = new Faker();
        String name = faker.name().fullName();
        String email = faker.internet().safeEmailAddress();
        int age = RANDOM.nextInt(16, 100);
        String password = "password";
        Gender gender = age % 2 == 0 ? Gender.MALE : Gender.FEMALE;

        MemberRegistrationRequest request = new MemberRegistrationRequest(
                name, age, email, password, gender
        );

        // POST request to client
        String jwtToken = webTestClient.post()
                    .uri(MEMBER_URI)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(request), MemberRegistrationRequest.class)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .returnResult(Void.class)
                    .getResponseHeaders()
                    .get(AUTHORIZATION)
                    .get(0);

        // get all members
        List<MemberDTO> members = webTestClient.get()
                .uri(MEMBER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<MemberDTO>() {
                })
                .returnResult()
                .getResponseBody();

        int id = members.stream()
                .filter(c -> c.email().equals(email))
                .map(MemberDTO::id)
                .findFirst()
                .orElseThrow();

        MemberDTO expectedMember = new MemberDTO(
                id,
                name,
                email,
                age,
                gender,
                List.of("ROLE_USER"),
                email,
                null);

        // verify POST member is present
        assertThat(members)
                .contains(expectedMember);

        // get member by id
        webTestClient.get()
                .uri(MEMBER_URI + "/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<MemberDTO>() {
                })
                .isEqualTo(expectedMember);
    }

    @Test
    void canDeleteMember(){
        // create registration request
        Faker faker = new Faker();
        String name = faker.name().fullName();
        String email = faker.internet().safeEmailAddress();
        int age = faker.random().nextInt(16,100);
        String password = "password";
        Gender gender = age % 2 == 0 ? Gender.MALE : Gender.FEMALE;

        MemberRegistrationRequest request = new MemberRegistrationRequest(
                name, age, email, password, gender
        );

        MemberRegistrationRequest request2 = new MemberRegistrationRequest(
                name, age, email + ".corp", password, gender
        );
        // POST registration request
        webTestClient.post()
                .uri(MEMBER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), MemberRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk();

        // POST registration request 2
        // saving request 2 jwtToken to keep api access after delete 1st member
        String jwtToken = webTestClient.post()
                .uri(MEMBER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request2), MemberRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Void.class)
                .getResponseHeaders()
                .get(AUTHORIZATION)
                .get(0);

        // verify member's registered or present in db
        List<MemberDTO> members = webTestClient.get()
                .uri(MEMBER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<MemberDTO>() {
                })
                .returnResult()
                .getResponseBody();

        int id = members.stream()
                .filter(c -> c.email().equals(email))
                .map(MemberDTO::id)
                .findFirst()
                .orElseThrow();

        // delete member 1
        webTestClient.delete()
                .uri(MEMBER_URI + "/{id}", id)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();

        // checks for delete member
        webTestClient.get()
                .uri(MEMBER_URI + "/{id}",id)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void canUpdateMember(){
        // create registration request
        Faker faker = new Faker();
        String name = faker.name().fullName();
        String email = faker.internet().safeEmailAddress();
        int age = faker.random().nextInt(16,100);
        String password = "password";
        Gender gender = age % 2 == 0 ? Gender.MALE : Gender.FEMALE;

        MemberRegistrationRequest request = new MemberRegistrationRequest(
                name, age, email, password, gender
        );

        // POST registration request
        String jwtToken = webTestClient.post()
                .uri(MEMBER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), MemberRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Void.class)
                .getResponseHeaders()
                .get(AUTHORIZATION)
                .get(0);

        // getting member id
        List<MemberDTO> members = webTestClient.get()
                .uri(MEMBER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<MemberDTO>() {
                })
                .returnResult()
                .getResponseBody();

        int id = members.stream()
                .filter(m -> m.email().equals(email))
                .map(MemberDTO::id)
                .findFirst()
                .orElseThrow();

        // create update request
        MemberUpdateRequest updateRequest = new MemberUpdateRequest(
                name+ " Lee",null,null, gender
        );

        // PUT update request
        webTestClient.put()
                .uri(MEMBER_URI+"/{id}",id)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updateRequest), MemberUpdateRequest.class)
                .exchange()
                .expectStatus()
                .isOk();

        // get updated member
        MemberDTO updatedMember = webTestClient.get()
                .uri(MEMBER_URI + "/{id}",id)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MemberDTO.class)
                .returnResult()
                .getResponseBody();

        // assert, age is unchanged
        MemberDTO expectedMember = new MemberDTO(id, name+ " Lee",email, age, gender, List.of("ROLE_USER"), email, null);

        assertThat(updatedMember).isEqualTo(expectedMember);
    }

    @Test
    void canUploadAndDownloadProfileImage() throws IOException {
        // create registration request
        Faker faker = new Faker();
        String name = faker.name().fullName();
        String email = faker.internet().safeEmailAddress();
        int age = faker.random().nextInt(16,100);
        String password = "password";
        Gender gender = age % 2 == 0 ? Gender.MALE : Gender.FEMALE;

        MemberRegistrationRequest request = new MemberRegistrationRequest(
                name, age, email, password, gender
        );

        // POST registration request
        String jwtToken = webTestClient.post()
                .uri(MEMBER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), MemberRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Void.class)
                .getResponseHeaders()
                .get(AUTHORIZATION)
                .get(0);

        // getting member id
        List<MemberDTO> members = webTestClient.get()
                .uri(MEMBER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<MemberDTO>() {
                })
                .returnResult()
                .getResponseBody();

        MemberDTO memberDTO = members.stream()
                .filter(m -> m.email().equals(email))
                .findFirst()
                .orElseThrow();

        assertThat(memberDTO.profileImageId()).isNullOrEmpty();

        String imageName = age % 2 == 0 ? "baby_yoda" : "storm_trooper";
        Resource image = new ClassPathResource("images/%s.jpg".formatted(imageName).toLowerCase());

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", image);

        // POST profile Image
        webTestClient.post()
                .uri(MEMBER_URI + "/{id}/profile-image", memberDTO.id())
                .accept(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isOk();
        // upload is successfull on S3
        // returning response error code 500

        // get saved profileImageId
        String profileImageId = webTestClient.get()
                .uri(MEMBER_URI + "/{id}", memberDTO.id())
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(MemberDTO.class)
                .returnResult()
                .getResponseBody()
                .profileImageId();

        assertThat(profileImageId).isNotBlank();

        // download profile image
        byte[] downloadedProfileImage = webTestClient.get()
                .uri(MEMBER_URI + "/{id}/profile-image", memberDTO.id())
                .accept(MediaType.IMAGE_JPEG)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(byte[].class)
                .returnResult()
                .getResponseBody();

        byte[] actual = Files.toByteArray(image.getFile());

        assertThat(actual).isEqualTo(downloadedProfileImage);
    }
}
