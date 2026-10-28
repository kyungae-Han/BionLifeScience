package com.dev.BionLifeScienceWeb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="notice_subject")
@Data
public class NoticeSubject {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="NOTICE_SUBJECT_ID")
	private Long id;
	
	@Column(name="NOTICE_SUBJECT_TEXT")
	private String text;
	
	/** 영문 분류명. 비우면 화면에서 한글이 나간다 */
	@Column(name="NOTICE_SUBJECT_TEXT_EN", length = 1000)
	private String textEn;

	/** 목록에 놀이는 순서. 작은 것이 앞이다.
	 *  인증서 분류의 SORT_INDEX 와 같은 방식이다 */
	@Column(name="SORT_INDEX")
	private Integer sortIndex;
	
}
