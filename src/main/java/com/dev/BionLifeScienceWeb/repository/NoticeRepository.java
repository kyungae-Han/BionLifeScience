package com.dev.BionLifeScienceWeb.repository;

import java.util.List;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
	
	
	@Query("select n from Notice n left join fetch n.noticeSubject where n.id = :id")
	Optional<Notice> findOneWithSubject(@Param("id") Long id);
	Optional<Notice> findTop1ByIdLessThanOrderByIdDesc(Long id);
	Optional<Notice> findTop1ByIdGreaterThanOrderByIdAsc(Long id);
}
