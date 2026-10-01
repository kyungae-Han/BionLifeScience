package com.dev.BionLifeScienceWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.dev.BionLifeScienceWeb.model.page.PageContent;
import com.dev.BionLifeScienceWeb.model.page.PageGroup;
import com.dev.BionLifeScienceWeb.repository.brand.BrandRepository;
import com.dev.BionLifeScienceWeb.repository.page.PageContentRepository;
import com.dev.BionLifeScienceWeb.repository.page.PageGroupRepository;
import com.dev.BionLifeScienceWeb.service.namecard.NameCardService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DynamicPageController {

    private final PageContentRepository pageContentRepository;
    private final PageGroupRepository pageGroupRepository;
    private final BrandRepository brandRepository;
    private final NameCardService nameCardService;


    @GetMapping("/{basePath:[^.]+}")
    public String groupList(@PathVariable String basePath, Model model) {
    	
    	
    	 	Optional<PageContent> pageOpt = pageContentRepository
    	            .findByPageGroup_BasePathAndSlugAndUseYn(basePath, basePath, "Y");

    	    if (pageOpt.isPresent()) {
    	        return renderPage(pageOpt.get(), model);
    	    }
    	
    	    Optional<PageGroup> groupOpt = pageGroupRepository.findByBasePathAndUseYn(basePath, "Y");

    	    // 이벤트 페이지가 아니면 디지털 명함 주소인지 본다.
    	    // 명함은 사내 메일 아이디를 그대로 쓴다 (dh_jang@... → /dh_jang).
    	    // 이 순서라 이미 쓰던 이벤트 페이지 주소를 명함이 가로챌 일은 없다.
    	    if (groupOpt.isEmpty()) {
    	        return nameCardService.findVisible(basePath)
    	                .map(card -> {
    	                    model.addAttribute("card", card);
    	                    return NameCardController.CARD_VIEW;
    	                })
    	                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    	    }

    	    PageGroup group = groupOpt.get();

    	    List<PageContent> pageList =
    	            pageContentRepository.findByPageGroup_PageGroupIdAndUseYnOrderByPageIndexAscPageContentIdDesc(
    	                    group.getPageGroupId(), "Y");

    	    model.addAttribute("group", group);
    	    model.addAttribute("pageList", pageList);
    	    
    	    
    	    return "front/eventPage/eventList";
    }
    

    /**
     * 그룹 주소와 슬러그가 다른 페이지는 두 칸 주소로 연다.
     *
     * 한 칸 주소(/{basePath}) 는 슬러그가 그룹 주소와 같은 페이지만 찾는다.
     * 그래서 한 그룹에 페이지를 여러 장 넣어도 두 번째부터는 갈 길이 없었다.
     * 이 길이 그 두 번째 페이지들을 맡는다.
     *
     * 슬러그가 그룹 주소와 같으면 지금까지 쓰던 한 칸 주소로 넘긴다.
     * 같은 화면이 주소 두 개로 열리면 검색 엔진이 중복으로 본다.
     *
     * 앞칸이 고정된 주소(/brandDetail/{id} 등)는 스프링이 그쪽을 먼저 고르므로
     * 이 길이 가로채지 않는다. 점이 들어간 주소는 정규식이 막는다.
     */
    @GetMapping("/{basePath:[^.]+}/{slug:[^.]+}")
    public String pageDetail(@PathVariable String basePath, @PathVariable String slug, Model model) {

        // DB 는 대소문자를 가리지 않고 찾는다 (/PA-micro-N 이 pa-micro-n 을 연다).
        // 자바에서만 가려 비교하면 DB 조회 결과와 어긋난다.
        if (basePath.equalsIgnoreCase(slug)) {
            return "redirect:/" + basePath;
        }

        PageContent page = pageContentRepository
                .findByPageGroup_BasePathAndSlugAndUseYn(basePath, slug, "Y")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return renderPage(page, model);
    }

    /** 한 칸 주소와 두 칸 주소가 같은 화면을 그리도록 한곳에 모은다. */
    private String renderPage(PageContent page, Model model) {
        model.addAttribute("group", page.getPageGroup());
        model.addAttribute("page", page);

        brandRepository.findByName(page.getPageName())
                .ifPresent(brand -> model.addAttribute("brandId", brand.getId()));

        // page_type 이 FULL 이면 공통 머리말 없이 그 페이지만 그린다.
        // 원본 HTML 을 그대로 옮겨 온 홍보 페이지처럼 자기 디자인을 가진 화면에 쓴다.
        if ("FULL".equalsIgnoreCase(page.getPageType())) {
            return "front/eventPage/pageDetailFull";
        }
        return "front/eventPage/pageDetail";
    }

}