package com.dev.BionLifeScienceWeb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="banner")
public class Banner {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="BANNER_ID")
	private Long id;
	
	@Column(name="BANNER_SUBJECT")
	private String subject;
	
	@Column(name="BANNER_CONTENT")
	private String content;
	
	@Column(name="BANNER_WEB_PATH")
	private String webpath;
	
	@Column(name="BANNER_WEB_NAME")
	private String webname;
	
	@Column(name="BANNER_WEB_ROAD")
	private String webroad;
	
	@Column(name="BANNER_MOBILE_NAME")
	private String mobilename;
	
	@Column(name="BANNER_MOBILE_PATH")
	private String mobilepath;
	
	@Column(name="BANNER_MOBILE_ROAD")
	private String mobileroad;
}










