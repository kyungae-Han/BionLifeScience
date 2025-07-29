package com.dev.BionLifeScienceWeb.model;

import java.util.Date;

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
	
	@Column(name="NOTICE_DATE")
	private Date date;
	
	@Column(name="NOTICE_CONTENT")
	private String content;
	
	@Column(name="NOTICE_SIGN")
	private Boolean sign;
	
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
