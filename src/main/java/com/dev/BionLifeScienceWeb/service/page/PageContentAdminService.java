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

    /**
     * 사용 안 함으로 바꾼다. 데이터는 남아 있고 화면에서만 빠진다.
     * 수정 화면에서 다시 사용으로 되돌릴 수 있다.
     */
    @Transactional
    public void delete(Long pageContentId) {
        PageContent entity = getDetail(pageContentId);
        entity.setUseYn("N");
    }

    /**
     * 페이지를 DB 에서 실제로 지운다. 되돌릴 수 없다.
     *
     * 안전장치: 사용 안 함(use_yn = 'N') 인 것만 지울 수 있다.
     * 쓰고 있는 페이지를 실수로 지우는 일을 막는다. 먼저 사용 안 함으로 바꿔야 한다.
     *
     * 올려둔 비주얼 이미지 파일은 업로드 폴더에 그대로 남는다 (사용자 데이터라 건드리지 않는다).
     */
    @Transactional
    public void deleteForever(Long pageContentId) {
        PageContent entity = getDetail(pageContentId);

        if (!"N".equalsIgnoreCase(nvl(entity.getUseYn()).trim())) {
            throw new IllegalArgumentException(
                    "사용 중인 페이지는 지울 수 없습니다. 먼저 '사용 안 함' 으로 바꿔 주세요.");
        }

        pageContentRepository.delete(entity);
    }

    /**
     * 그룹을 그 안의 페이지와 함께 DB 에서 실제로 지운다. 되돌릴 수 없다.
     *
     * 안전장치: 그룹 안의 페이지가 전부 사용 안 함(N) 이어야 지울 수 있다.
     * 하나라도 쓰고 있으면 거부한다.
     *
     * 페이지가 그룹을 참조하므로 페이지를 먼저 지워야 한다.
     * 순서를 바꾸면 외래키 제약에 걸린다.
     *
     * @return 같이 지워진 페이지 수
     */
    @Transactional
    public int deleteGroupForever(Long pageGroupId) {
        PageGroup group = pageGroupRepository.findById(pageGroupId)
                .orElseThrow(() -> new IllegalArgumentException("페이지 그룹 정보가 없습니다."));

        List<PageContent> pages =
                pageContentRepository.findByPageGroup_PageGroupIdOrderByPageIndexAscPageContentIdDesc(pageGroupId);

        long using = pages.stream()
                .filter(p -> !"N".equalsIgnoreCase(nvl(p.getUseYn()).trim()))
                .count();

        if (using > 0) {
            throw new IllegalArgumentException(
                    "이 그룹에 사용 중인 페이지가 " + using + "개 있습니다. "
                    + "전부 '사용 안 함' 으로 바꾼 뒤에 지울 수 있습니다.");
        }

        pageContentRepository.deleteAll(pages);
        pageGroupRepository.delete(group);
        return pages.size();
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