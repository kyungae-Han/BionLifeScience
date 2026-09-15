package com.dev.BionLifeScienceWeb.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>{
	
	Optional<Member> findByUsername(String username);
	
	Optional<Member> findByPhone(String phone);

	/** 권한별 계정 수. 마지막 관리자를 내리거나 지우지 못하게 막을 때 쓴다 */
	long countByRole(String role);
	
	
}
