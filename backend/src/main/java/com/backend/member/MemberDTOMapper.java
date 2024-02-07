package com.backend.member;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MemberDTOMapper implements Function<Member, MemberDTO> {

    @Override
    public MemberDTO apply(Member member) {
        return new MemberDTO(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getAge(),
                member.getGender(),
                member.getAuthorities()
                        .stream().map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()),
                member.getUsername(),
                member.getProfileImageId()
        );
    }
}
