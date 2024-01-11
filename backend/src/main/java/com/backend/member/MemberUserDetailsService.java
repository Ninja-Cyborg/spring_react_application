package com.backend.member;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberDao memberDao;

    public MemberUserDetailsService(@Qualifier("jpa") MemberDao memberDao){
        this.memberDao = memberDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberDao.selectUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username: " + username + " not found."));
    }
}
