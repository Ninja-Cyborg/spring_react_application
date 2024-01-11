package com.backend.member;

public record MemberRegistrationRequest(
        String name,
        Integer age,
        String email,
        String password,
        Gender gender) {
}
