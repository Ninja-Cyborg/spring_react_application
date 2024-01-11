package com.backend.member;

public record MemberUpdateRequest(
        String name,
        String email,
        Integer age,
        Gender gender) {
}
