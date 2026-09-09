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
    	        PageContent page = pageOpt.get();
    	        model.addAttribute("group", page.getPageGroup());
    	        model.addAttribute("page", page);
    	        
    	        brandRepository.findByName(page.getPageName())
    	        .ifPresent(brand -> model.addAttribute("brandId", brand.getId()));
    	        
    	        
    	        return "front/eventPage/pageDetail";
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
    
}