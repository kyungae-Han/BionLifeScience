package com.dev.BionLifeScienceWeb.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dev.BionLifeScienceWeb.model.Member;
import com.dev.BionLifeScienceWeb.model.MemberAccount;
import com.dev.BionLifeScienceWeb.repository.MemberRepository;

@Service
@Configuration
public class MemberService implements UserDetailsService {

	@Bean(name = "saveBean")
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Autowired
	private MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("loadUserByUsername");
		Optional<Member> member = memberRepository.findByUsername(username);
		if (!member.isPresent()) {
			throw new UsernameNotFoundException("NONE");
		}

		MemberAccount mem = new MemberAccount(member.get());

		return mem;
	}

	public Member insertAdmin(Member member) {
		String encodedPassword = passwordEncoder().encode(member.getPassword());
		member.setPassword(encodedPassword);
		member.setEnabled(true);
		member.setRole("ROLE_ADMIN");
		return memberRepository.save(member);

	}
}
