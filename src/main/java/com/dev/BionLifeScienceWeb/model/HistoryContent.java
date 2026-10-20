package com.dev.BionLifeScienceWeb.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="history_content")
public class HistoryContent {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="HISTORY_CONTENT_ID")
	private Long id;
	
	@Column(name="HISTORY_CONTENT_SUBJECT")
	private String contentSubject;

	/** 연혁 내용 영문. 비워 두면 영문 화면에서도 한글이 나간다 (@enText.pick) */
	@Column(name="HISTORY_CONTENT_SUBJECT_EN")
	private String contentSubjectEn;

	@Column(name="HISTORY_CONTENT_DATE")
	private Date date;
	
	@Column(name="HISTORY_REFER_ID")
	private Long subjectId;
	
	@Column(name="HISTORY_STRING_DATE")
	private String historyDate;
	
}





































