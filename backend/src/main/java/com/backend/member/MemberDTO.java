package com.backend.member;

import java.util.List;

public record MemberDTO(
        Integer id,
        String name,
        String email,
        Integer age,
        Gender gender,
        List<String> roles,
        String username,
        String profileImageId) {
}
