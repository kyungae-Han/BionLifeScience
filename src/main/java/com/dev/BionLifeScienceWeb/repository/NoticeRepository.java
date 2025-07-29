package com.dev.BionLifeScienceWeb.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.Notice;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long>{

	Page<Notice> findAllByOrderByDateDesc(Pageable pageable);
	
	List<Notice> findAllBySignOrderByDateDesc(Boolean sign);
	
	List<Notice> findBySubjectContains(String subject);
	
	List<Notice> findTop5ByOrderBySignDescDateDesc();
	
	List<Notice> findAllBySubjectContainsOrderBySignDescDateDesc(String subject);
}
