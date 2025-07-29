package com.dev.BionLifeScienceWeb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.HistoryContent;

@Repository
public interface HistoryContentRepository extends JpaRepository<HistoryContent, Long>{
	
	List<HistoryContent> findAllBySubjectIdOrderByDateDesc(Long id);
	
	int deleteAllBySubjectId(Long id);
}
