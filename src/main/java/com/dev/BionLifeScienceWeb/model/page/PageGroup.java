package com.dev.BionLifeScienceWeb.model.page;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "page_group")
@Getter
@Setter
public class PageGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "page_group_id")
    private Long pageGroupId;

    @Column(name = "group_name", nullable = false, length = 200)
    private String groupName;

    @Column(name = "base_path", nullable = false, unique = true, length = 100)
    private String basePath;

    @Column(name = "group_desc", length = 1000)
    private String groupDesc;

    @Column(name = "group_index", nullable = false)
    private Integer groupIndex = 0;

    @Column(name = "use_yn", nullable = false, length = 1)
    private String useYn = "Y";
}
