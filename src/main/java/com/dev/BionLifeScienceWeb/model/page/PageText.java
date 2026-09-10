package com.dev.BionLifeScienceWeb.model.page;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 화면에 박혀 있던 고정 문구. 한 줄이 문구 하나다.
 *
 * 템플릿에 그대로 적혀 있던 글을 여기로 옮기면 관리자에서 고칠 수 있고
 * 영문 칸을 채우면 영문 화면에서 바뀐다.
 *
 * 화면에서 이렇게 쓴다.
 *   th:text="${@pageText.get('rnd', 'rnd.title')} ?: _"
 *
 * 값이 없으면 null 이 나오고 ?: _ 때문에 템플릿에 적힌 원래 글이 그대로 남는다.
 * 그래서 DB 를 아직 안 채웠거나 줄을 지워도 화면이 비지 않는다.
 */
@Entity
@Table(name = "page_text")
@Getter
@Setter
public class PageText {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "page_text_id")
	private Long pageTextId;

	/** 어느 화면인지. rnd, about ... */
	@Column(name = "page_code", nullable = false, length = 50)
	private String pageCode;

	/** 화면 안에서 문구를 가리키는 이름. rnd.card1.title */
	@Column(name = "text_key", nullable = false, length = 100)
	private String textKey;

	@Column(name = "text_ko", columnDefinition = "TEXT")
	private String textKo;

	@Column(name = "text_en", columnDefinition = "TEXT")
	private String textEn;

	/** 관리자 화면에 보여 줄 설명. 어느 자리 글인지 알려 준다 */
	@Column(name = "label", length = 255)
	private String label;

	/** 관리자 화면에서 묶어 보여 줄 구역 이름. 개요, 제품개발 단계 ... */
	@Column(name = "section", length = 50)
	private String section;

	/** true 면 값에 태그가 들어 있다. 관리자 화면에서 알려 주기만 한다 */
	@Column(name = "html_allowed", nullable = false)
	private boolean htmlAllowed;

	@Column(name = "sort_index", nullable = false)
	private int sortIndex;
}
