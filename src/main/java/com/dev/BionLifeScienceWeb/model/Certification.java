package com.dev.BionLifeScienceWeb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="certification")
@Data
public class Certification {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="CERTIFICATION_ID")
	private Long id;

	@Column(name="CERTIFICATION_SUBJECT")
	private String subject;

	/** 영문 화면에서 쓰는 제목. 비어 있으면 한글이 그대로 나간다 */
	@Column(name="CERTIFICATION_SUBJECT_EN")
	private String subjectEn;
	
	@Column(name="CERTIFICATION_CONTENT")
	private String content;

	@Column(name="CERTIFICATION_CONTENT_EN")
	private String contentEn;
	
	/**
	 * 예전 방식의 분류 이름. 지금은 certification_sort 가 참이고 이 칸은 그 사본이다.
	 * 저장할 때 고른 분류의 한글 이름을 같이 넣어 둔다. 지우지 않는 이유는
	 * 이 값을 읽는 곳이 더 남아 있을 수 있어서다. 화면 분기는 sortId 로만 한다
	 */
	@Column(name="CERTIFICATION_SORT")
	private String sort;

	/** 고른 분류. 등록·수정 폼의 select 가 이 칸으로 들어온다 */
	@Column(name="CERTIFICATION_SORT_ID")
	private Long sortId;

	/**
	 * 분류 이름을 꺼내 쓰려고 붙였다. 쓰기는 위의 sortId 가 한다.
	 * 그래서 insertable/updatable 을 꺼 둔다. 한 칸을 둘이 쓰면 하이버네이트가 막는다
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="CERTIFICATION_SORT_ID", insertable=false, updatable=false)
	private CertificationSort certificationSort;

	@Column(name="CERTIFICATION_IMAGE_ROAD")
	private String road;
	
	@Column(name="CERTIFICATION_IMAGE_PATH")
	private String path;

	/** 목록에서 끌어 맞춘 순서. 작은 것이 앞이다. 배너의 BANNER_INDEX 와 같은 방식이다 */
	@Column(name="CERTIFICATION_INDEX")
	private Integer certificationIndex;
}
