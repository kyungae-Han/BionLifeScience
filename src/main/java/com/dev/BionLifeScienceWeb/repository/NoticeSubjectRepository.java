package com.dev.BionLifeScienceWeb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.NoticeSubject;

@Repository
public interface NoticeSubjectRepository extends JpaRepository<NoticeSubject, Long>{

	
}
