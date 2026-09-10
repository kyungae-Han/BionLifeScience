package com.dev.BionLifeScienceWeb.model;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Table(name="notice")
@Data
public class Notice {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="NOTICE_ID")
	private Long id;
	
	@Column(name="NOTICE_SUBJECT")
	private String subject;
	
	/** 영문 제목. 비우면 화면에서 한글이 나간다 */
	@Column(name="NOTICE_SUBJECT_EN", length = 1000)
	private String subjectEn;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(name="NOTICE_DATE")
	private Date date;
	
	@Column(name="NOTICE_CONTENT")
	private String content;
	
	/** 영문 본문. 비우면 화면에서 한글이 나간다 */
	@Column(name="NOTICE_CONTENT_EN", columnDefinition = "LONGTEXT")
	private String contentEn;
	
	@Column(name="NOTICE_SIGN")
	private Boolean sign;
	
	@Column(name="NOTICE_IMAGE_URL", length=500)
	private String imageUrl;
	
	@Transient
	private Long subjectId;
	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(
			name="NOTICE_REFER_ID", referencedColumnName="NOTICE_SUBJECT_ID"
			)
	private NoticeSubject noticeSubject;
	
	@Transient
	private String subjectText;
	
}
