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
	
	
	// 같은 분류 내에서 이전글(현재보다 작은 ID 중 가장 가까운 것 = 더 오래된 글)
    Optional<Notice> findTopByNoticeSubject_IdAndIdLessThanOrderByIdDesc(Long subjectId, Long id);
    
 // 같은 분류 내에서 다음글(현재보다 큰 ID 중 가장 가까운 것 = 더 최신 글)
    Optional<Notice> findTopByNoticeSubject_IdAndIdGreaterThanOrderByIdAsc(Long subjectId, Long id);
    
    List<Notice> findTop5BySignOrderByDateDesc(boolean sign);
    
    Page<Notice> findBySignFalse(Pageable pageable);
    
    Page<Notice> findAllByOrderByIdDesc(String q, Pageable pageable);
    
	 Page<Notice> findBySubjectContainingIgnoreCaseOrContentContainingIgnoreCase(
	     String q1, String q2, Pageable pageable);
}
