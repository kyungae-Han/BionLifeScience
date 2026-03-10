package com.dev.BionLifeScienceWeb.model.page;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "page_content")
@Getter
@Setter
public class PageContent {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "page_content_id")
    private Long pageContentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_group_id", nullable = false)
    private PageGroup pageGroup;

    @Column(name = "page_name", nullable = false, length = 1000)
    private String pageName;

    @Column(name = "page_sub_name", length = 1000)
    private String pageSubName;

    @Column(name = "page_content", length = 1000)
    private String pageContent;

    @Column(name = "page_index")
    private Integer pageIndex;

    @Column(name = "page_type", length = 20)
    private String pageType;

    @Column(name = "slug", nullable = false, length = 255, unique = true)
    private String slug;

    @Column(name = "page_visual_path", length = 255)
    private String pageVisualPath;

    @Column(name = "page_visual_road", length = 255)
    private String pageVisualRoad;

    @Column(name = "page_visual_name", length = 255)
    private String pageVisualName;

    @Lob
    @Column(name = "page_desc")
    private String pageDesc;

    @Column(name = "use_yn", nullable = false, length = 1)
    private String useYn = "Y";
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
