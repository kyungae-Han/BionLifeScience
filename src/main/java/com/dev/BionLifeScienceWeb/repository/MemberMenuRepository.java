package com.dev.BionLifeScienceWeb.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dev.BionLifeScienceWeb.model.MemberMenu;

@Repository
public interface MemberMenuRepository extends JpaRepository<MemberMenu, Long> {

	List<MemberMenu> findByMemberId(Long memberId);

	List<MemberMenu> findByMemberIdIn(Collection<Long> memberIds);

	@Modifying
	@Transactional
	void deleteByMemberId(Long memberId);
}
