package com.dev.BionLifeScienceWeb.handler;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dev.BionLifeScienceWeb.model.Member;
import com.dev.BionLifeScienceWeb.model.MemberAccount;
import com.dev.BionLifeScienceWeb.service.MemberService;

@Configuration
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private MemberService userDetailsService;

    @Bean(name="AuthEncoder")
	PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    	
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();
        MemberAccount memberAccount = (MemberAccount) userDetailsService.loadUserByUsername(username);
        if (memberAccount.getMember()==null) {
        	System.out.println("Account is None");
            throw new BadCredentialsException("NONE");
        }
        Member member = memberAccount.getMember();
        if (!passwordEncoder().matches(password, member.getPassword())) {
        	System.out.println("Password not match");
            throw new BadCredentialsException("PWER");
        }
        

        if (!member.getEnabled()) {
        	System.out.println("not CertificationTT");
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
