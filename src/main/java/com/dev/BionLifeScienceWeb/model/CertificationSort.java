package com.dev.BionLifeScienceWeb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 인증서 분류. 회사소개와 인증서 페이지의 탭이 이 표에서 나온다.
 * 공지사항의 notice_subject 와 같은 자리의 것이다.
 *
 * 예전에는 분류가 템플릿 네 군데에 한글로 박혀 있어서, 분류를 하나 늘리려면
 * 코드를 고쳐 다시 배포해야 했다. 게다가 회사소개는 pf-기업인증 처럼 한글을
 * 클래스에 그대로 쓰고 인증서 페이지는 pf-icons 로 바꿔 쓰는 등 규칙이 둘이었다.
 * 이제 두 화면 모두 pf-{id} 하나로 간다.
 */
@Entity
@Table(name = "certification_sort")
@Data
public class CertificationSort {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CERTIFICATION_SORT_ID")
	private Long id;

	@Column(name = "NAME_KO", nullable = false, length = 100)
	private String nameKo;

	/** 영문 화면에서 쓰는 이름. 비어 있으면 한글이 그대로 나간다 */
	@Column(name = "NAME_EN", length = 200)
	private String nameEn;

	/** 탭에 놓이는 순서. 작은 것이 앞이다. 인증서의 CERTIFICATION_INDEX 와 같은 방식이다 */
	@Column(name = "SORT_INDEX")
	private Integer sortIndex;
}
