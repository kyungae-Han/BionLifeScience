package com.dev.BionLifeScienceWeb.repository.page;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.BionLifeScienceWeb.model.page.PageContent;

public interface PageContentRepository extends JpaRepository<PageContent, Long> {

	List<PageContent> findAllByOrderByPageIndexAscPageContentIdDesc();
	List<PageContent> findByPageGroup_PageGroupIdOrderByPageIndexAscPageContentIdDesc(Long pageGroupId);
    List<PageContent> findByPageGroup_PageGroupIdAndUseYnOrderByPageIndexAscPageContentIdDesc(Long pageGroupId, String useYn);
    
    Optional<PageContent> findByPageGroup_BasePathAndSlugAndUseYn(String basePath, String slug, String useYn);
    
    boolean existsByPageGroup_PageGroupIdAndSlug(Long pageGroupId, String slug);
    boolean existsByPageGroup_PageGroupIdAndSlugAndPageContentIdNot(Long pageGroupId, String slug, Long pageContentId);
}
