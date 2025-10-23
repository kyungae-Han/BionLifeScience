package com.dev.BionLifeScienceWeb.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
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

	    /** 로그인 시 DB 조회 */
	    @Override
	    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	        Optional<Member> member = memberRepository.findByUsername(username);
	        if (!member.isPresent()) {
	            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
	        }
	        return new MemberAccount(member.get());
	    }

	    /** ✅ 관리자 전용 등록 */
	    public Member insertAdmin(Member member) {
	        String encodedPassword = passwordEncoder().encode(member.getPassword());
	        member.setPassword(encodedPassword);
	        member.setEnabled(true);
	        member.setRole("ROLE_ADMIN");
	        return memberRepository.save(member);
	    }

	    /** ✅ 일반 회원 등록 (role을 폼 입력값으로 받음) */
	    public Member insertMember(Member member) {
	        String encodedPassword = passwordEncoder().encode(member.getPassword());
	        member.setPassword(encodedPassword);
	        member.setEnabled(true);

	        // 폼에서 선택한 role 그대로 저장, 없으면 기본값 ROLE_USER
	        if (member.getRole() == null || member.getRole().isEmpty()) {
	            member.setRole("ROLE_USER");
	        }

	        return memberRepository.save(member);
	    }
	    
	    /** ✅ 전체 멤버 목록 조회 */
	    public List<Member> getAllMembers() {
	        return memberRepository.findAll(Sort.by(Sort.Direction.ASC, "username"));
	    }

	    /** ✅ 특정 멤버 삭제 */
	    public void deleteMember(Long id) {
	        memberRepository.deleteById(id);
	    }
	    
}
