package com.backend.auth;

public record AuthenticationRequest(
        String username,
        String password
) {

}
