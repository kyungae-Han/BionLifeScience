package com.dev.BionLifeScienceWeb.service.page;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev.BionLifeScienceWeb.model.page.PageGroup;
import com.dev.BionLifeScienceWeb.repository.page.PageGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PageGroupAdminService {

    private final PageGroupRepository pageGroupRepository;

    public List<PageGroup> getList() {
        return pageGroupRepository.findAllByOrderByGroupIndexAscPageGroupIdDesc();
    }

    public PageGroup getDetail(Long pageGroupId) {
        return pageGroupRepository.findById(pageGroupId)
                .orElseThrow(() -> new IllegalArgumentException("페이지 그룹 정보가 없습니다."));
    }

    @Transactional
    public Long save(PageGroup form) {
        validate(form);

        PageGroup entity;
        if (form.getPageGroupId() != null) {
            entity = getDetail(form.getPageGroupId());
        } else {
            entity = new PageGroup();
        }

        entity.setGroupName(nvl(form.getGroupName()));
        entity.setBasePath(nvl(form.getBasePath()).trim());
        entity.setGroupDesc(form.getGroupDesc());
        entity.setGroupIndex(form.getGroupIndex() == null ? 0 : form.getGroupIndex());
        entity.setUseYn(isBlank(form.getUseYn()) ? "Y" : form.getUseYn());

        return pageGroupRepository.save(entity).getPageGroupId();
    }

    private void validate(PageGroup form) {
        if (isBlank(form.getGroupName())) {
            throw new IllegalArgumentException("그룹명을 입력해주세요.");
        }
        if (isBlank(form.getBasePath())) {
            throw new IllegalArgumentException("URL 경로(basePath)를 입력해주세요.");
        }
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}