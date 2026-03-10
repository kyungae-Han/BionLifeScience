package com.dev.BionLifeScienceWeb.repository.page;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.BionLifeScienceWeb.model.page.PageGroup;

public interface PageGroupRepository extends JpaRepository<PageGroup, Long> {
    
    List<PageGroup> findAllByOrderByGroupIndexAscPageGroupIdDesc();
    List<PageGroup> findByUseYnOrderByGroupIndexAscPageGroupIdDesc(String useYn);
    
    Optional<PageGroup> findByBasePathAndUseYn(String basePath, String useYn);
}
