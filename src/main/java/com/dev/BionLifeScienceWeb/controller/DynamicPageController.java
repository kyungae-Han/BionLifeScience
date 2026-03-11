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

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DynamicPageController {

    private final PageContentRepository pageContentRepository;
    private final PageGroupRepository pageGroupRepository;
    private final BrandRepository brandRepository;


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
    	
    	    PageGroup group = pageGroupRepository.findByBasePathAndUseYn(basePath, "Y")
    	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    	    List<PageContent> pageList =
    	            pageContentRepository.findByPageGroup_PageGroupIdAndUseYnOrderByPageIndexAscPageContentIdDesc(
    	                    group.getPageGroupId(), "Y");

    	    model.addAttribute("group", group);
    	    model.addAttribute("pageList", pageList);
    	    
    	    
    	    return "front/eventPage/eventList";
    }
    
}