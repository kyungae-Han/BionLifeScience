package com.dev.BionLifeScienceWeb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="company_info")
@Data
public class CompanyInfo {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="COMPANY_ID")
	private Long id;
	
	@Column(name="COMPANY_NAME")
	private String companyName;
	
	@Column(name="COMPANY_ADDRESS")
	private String companyAddress;
	
	@Column(name="COMPANY_NUMBER")
	private String companyNumber;
	
	@Column(name="COMPANY_TELEPHONE")
	private String companyTelephone;
	
	@Column(name="COMPANY_EMAIL")
	private Boolean companyEmailCheck;

	/**
	 * 회사소개 상단 배너 (한글). /upload/ 로 시작하는 주소다.
	 * 비우면 앞단이 기본 그림(/front/images/about/about-bion.jpg)을 쓴다.
	 */
	@Column(name="COMPANY_ABOUT_BANNER")
	private String aboutBanner;

	/**
	 * 회사소개 상단 배너 (영문).
	 * 비우면 한글 것을 쓰고, 그것도 없으면 기본 그림을 쓴다.
	 */
	@Column(name="COMPANY_ABOUT_BANNER_EN")
	private String aboutBannerEn;
	
}
