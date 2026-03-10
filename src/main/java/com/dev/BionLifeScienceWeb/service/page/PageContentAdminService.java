package com.dev.BionLifeScienceWeb.service.page;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.page.PageContent;
import com.dev.BionLifeScienceWeb.model.page.PageGroup;
import com.dev.BionLifeScienceWeb.repository.page.PageContentRepository;
import com.dev.BionLifeScienceWeb.repository.page.PageGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PageContentAdminService {


	@Value("${spring.upload.env}")
	private String env;
	
	
	@Value("${spring.upload.path}")
	private String commonPath;
	
	
	private static final String PAGE_UPLOAD_URL_PREFIX = "/upload/page";

    private final PageContentRepository pageContentRepository;
    private final PageGroupRepository pageGroupRepository;

    public List<PageContent> getList(Long groupId) {
        if (groupId != null) {
            return pageContentRepository.findByPageGroup_PageGroupIdOrderByPageIndexAscPageContentIdDesc(groupId);
        }
        return pageContentRepository.findAllByOrderByPageIndexAscPageContentIdDesc();
    }

    public PageContent getDetail(Long pageContentId) {
        return pageContentRepository.findById(pageContentId)
                .orElseThrow(() -> new IllegalArgumentException("페이지 정보가 없습니다."));
    }

    public List<PageGroup> getGroupList() {
        return pageGroupRepository.findAllByOrderByGroupIndexAscPageGroupIdDesc();
    }

    @Transactional
    public Long save(PageContent form, MultipartFile visualFile) {
        validateRequired(form);
        validateSlugDuplicate(form);

        PageContent entity;
        if (form.getPageContentId() != null) {
            entity = getDetail(form.getPageContentId());
        } else {
            entity = new PageContent();
        }

        PageGroup pageGroup = pageGroupRepository.findById(form.getPageGroup().getPageGroupId())
                .orElseThrow(() -> new IllegalArgumentException("페이지 그룹 정보가 없습니다."));
        
        String slug = nvl(form.getSlug()).trim().toLowerCase();
        
        if(!slug.matches("^[a-z0-9_-]+$")) {
        	 	throw new IllegalArgumentException("slug는 영문, 숫자, -, _ 만 사용할 수 있습니다.");
        }

        entity.setPageGroup(pageGroup);
        entity.setPageName(nvl(form.getPageName()));
        entity.setPageSubName(nvl(form.getPageSubName()));
        entity.setPageContent(nvl(form.getPageContent()));
        entity.setPageType(isBlank(form.getPageType()) ? "PAGE" : form.getPageType().trim());
        entity.setSlug(slug);
        entity.setPageDesc(form.getPageDesc());
        entity.setPageIndex(form.getPageIndex() == null ? 0 : form.getPageIndex());
        entity.setUseYn(isBlank(form.getUseYn()) ? "Y" : form.getUseYn());

        if (visualFile != null && !visualFile.isEmpty()) {
            uploadVisualImage(entity, visualFile);
        }

        return pageContentRepository.save(entity).getPageContentId();
    }

    @Transactional
    public void delete(Long pageContentId) {
        PageContent entity = getDetail(pageContentId);
        entity.setUseYn("N");
    }

    private void validateRequired(PageContent form) {
        if (form.getPageGroup() == null || form.getPageGroup().getPageGroupId() == null) {
            throw new IllegalArgumentException("페이지 그룹을 선택해주세요.");
        }
        if (isBlank(form.getPageName())) {
            throw new IllegalArgumentException("페이지명을 입력해주세요.");
        }
        if (isBlank(form.getSlug())) {
            throw new IllegalArgumentException("slug를 입력해주세요.");
        }
    }

    private void validateSlugDuplicate(PageContent form) {
        Long groupId = form.getPageGroup().getPageGroupId();
        String slug = form.getSlug().trim();

        boolean duplicated;
        if (form.getPageContentId() == null) {
            duplicated = pageContentRepository.existsByPageGroup_PageGroupIdAndSlug(groupId, slug);
        } else {
            duplicated = pageContentRepository.existsByPageGroup_PageGroupIdAndSlugAndPageContentIdNot(
                    groupId, slug, form.getPageContentId());
        }

        if (duplicated) {
            throw new IllegalArgumentException("같은 그룹 내 동일한 slug가 이미 존재합니다.");
        }
    }

    private void uploadVisualImage(PageContent entity, MultipartFile visualFile) {
        String originalName = visualFile.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("비주얼 이미지 파일명이 올바르지 않습니다.");
        }

        String lowerName = originalName.toLowerCase();
        if (!(lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png"))) {
            throw new IllegalArgumentException("비주얼 이미지는 jpg, jpeg, png 파일만 업로드 가능합니다.");
        }

        String datePath = LocalDate.now().toString();

        String slug = nvl(entity.getSlug()).trim();
        if (slug.isEmpty()) {
        	throw new IllegalArgumentException("slug 정보가 없습니다.");
        }
        
        Path basePath = Paths.get(commonPath).toAbsolutePath().normalize();
        Path dirPath = basePath.resolve("page").resolve(slug).resolve(datePath);

        File dir = dirPath.toFile();
        
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("비주얼 이미지 업로드 폴더 생성에 실패했습니다. path=" + dir.getAbsolutePath());
        }

        String ext = originalName.substring(originalName.lastIndexOf("."));
        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;

        File dest = dirPath.resolve(savedName).toFile();
        

        try {
            visualFile.transferTo(dest);
        } catch (IOException e) {
            throw new IllegalStateException("비주얼 이미지 저장에 실패했습니다. path=" + dest.getAbsolutePath(), e);
        }

        entity.setPageVisualName(savedName);
        entity.setPageVisualPath(dest.getAbsolutePath().replace("\\", "/"));
        entity.setPageVisualRoad(PAGE_UPLOAD_URL_PREFIX +"/"+slug+ "/" + datePath + "/" + savedName);
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}