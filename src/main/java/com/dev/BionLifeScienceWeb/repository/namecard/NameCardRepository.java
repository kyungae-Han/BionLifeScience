package com.dev.BionLifeScienceWeb.repository.namecard;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.namecard.NameCard;

@Repository
public interface NameCardRepository extends JpaRepository<NameCard, Long> {

	/** 화면이 거래 서류까지 함께 그리므로 한 번에 가져온다 */
	@EntityGraph(attributePaths = "documents")
	Optional<NameCard> findBySlugAndUseYn(String slug, String useYn);

	Optional<NameCard> findBySlug(String slug);

	boolean existsBySlug(String slug);

	List<NameCard> findAllByOrderByNameCardIdDesc();
}
