package com.dev.BionLifeScienceWeb.repository.page;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.BionLifeScienceWeb.model.page.PageText;

public interface PageTextRepository extends JpaRepository<PageText, Long> {

	List<PageText> findByPageCodeOrderBySortIndexAsc(String pageCode);

	List<PageText> findAllByOrderByPageCodeAscSortIndexAsc();
}
