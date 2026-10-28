package com.dev.BionLifeScienceWeb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.NoticeSubject;

@Repository
public interface NoticeSubjectRepository extends JpaRepository<NoticeSubject, Long>{

	/**
	 * 목록에 놀이는 순서대로 읽는다.
	 * 순서 값이 비어 있는 행은 마리아DB 규칙상 먼저 나온다.
	 * 마이그레이션 SQL 이 기존 분류를 모두 채워 넣는다
	 */
	java.util.List<NoticeSubject> findAllByOrderBySortIndexAscIdAsc();

	/** 같은 이름이 또 들어오는 것을 막는다 */
	boolean existsByText(String text);
}
