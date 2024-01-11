package com.backend.auth;

import com.backend.member.MemberDTO;

public record AuthenticationResponse (
        String token,
        MemberDTO memberDTO
) {
}
