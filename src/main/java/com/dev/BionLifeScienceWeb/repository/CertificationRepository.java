package com.dev.BionLifeScienceWeb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.Certification;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long>{

	/**
	 * 순서대로 읽는다. 순서 값이 비어 있는 행은 마리아DB 규칙상 먼저 나온다.
	 * 마이그레이션 SQL 이 기존 행을 모두 채워 넣으므로 평소엔 빈 값이 없다.
	 */
	List<Certification> findAllByOrderByCertificationIndexAscIdAsc();
}
