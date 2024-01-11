package com.backend.integration;

import com.backend.auth.AuthenticationRequest;
import com.backend.auth.AuthenticationResponse;
import com.backend.jwt.JWTUtil;
import com.backend.member.Gender;
import com.backend.member.MemberDTO;
import com.backend.member.MemberRegistrationRequest;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AuthenticationIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private JWTUtil jwtUtil;

    private static final String AUTHENTICATION_URI = "api/v1/auth";
    private static final String MEMBER_URI = "/api/v1/members";
    private static final Random RANDOM = new Random();

    @Test
    void canLogin(){
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

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                email,
                password
        );

        // passing unregistered credentials
        webTestClient.post()
                .uri(AUTHENTICATION_URI + "/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(authenticationRequest), AuthenticationRequest.class)
                .exchange()
                .expectStatus()
                .isUnauthorized();

        // POST request : register member and store token
        webTestClient.post()
                .uri(MEMBER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), MemberRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk();


        // store the result body/token
        EntityExchangeResult<AuthenticationResponse> result = webTestClient.post()
                .uri(AUTHENTICATION_URI + "/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(authenticationRequest), AuthenticationRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<AuthenticationResponse>() {
                })
                .returnResult();

        String jwtToken = result.getResponseHeaders()
                .get(HttpHeaders.AUTHORIZATION)
                .get(0);

        AuthenticationResponse authenticationResponse = result.getResponseBody();
        MemberDTO memberDTO = authenticationResponse.memberDTO();

        // assertion for token validity
        assertThat(jwtUtil.isTokenValid(jwtToken,
                authenticationResponse.memberDTO().username()));

        // assertion for DTO fields
        assertThat(memberDTO.email()).isEqualTo(email);
        assertThat(memberDTO.age()).isEqualTo(age);
        assertThat(memberDTO.gender()).isEqualTo(gender);
        assertThat(memberDTO.username()).isEqualTo(email);
        assertThat(memberDTO.roles()).isEqualTo(List.of("ROLE_USER"));

    }
}