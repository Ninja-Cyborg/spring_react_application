package com.backend.auth;

import com.backend.jwt.JWTUtil;
import com.backend.member.Member;
import com.backend.member.MemberDTO;
import com.backend.member.MemberDTOMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final MemberDTOMapper memberDTOMapper;
    private final JWTUtil jwtUtil;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 MemberDTOMapper memberDTOMapper,
                                 JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.memberDTOMapper = memberDTOMapper;
        this.jwtUtil = jwtUtil;
    }

    public AuthenticationResponse login(AuthenticationRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );
        Member principal = (Member)authentication.getPrincipal();
        MemberDTO memberDTO = memberDTOMapper.apply(principal);
        String token = jwtUtil.issueToken(memberDTO.username(), memberDTO.roles());

        return new AuthenticationResponse(token, memberDTO);
    }
}
