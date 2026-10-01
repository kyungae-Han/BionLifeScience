package com.dev.BionLifeScienceWeb.handler;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.dev.BionLifeScienceWeb.model.Member;
import com.dev.BionLifeScienceWeb.model.MemberAccount;
import com.dev.BionLifeScienceWeb.service.MemberService;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private MemberService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder; // 빈 주입

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    	
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        MemberAccount memberAccount = (MemberAccount) userDetailsService.loadUserByUsername(username);
        if (memberAccount.getMember()==null) {
            throw new BadCredentialsException("NONE");
        }
        Member member = memberAccount.getMember();
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BadCredentialsException("PWER");
        }
        

        if (!member.getEnabled()) {
            throw new DisabledException("not CertificationTT"+member.getUsername());
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
        		member,
                null,
                memberAccount.getAuthorities());
        return authenticationToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
