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
	
	/** 영문 제목. 비우면 화면에서 한글이 나간다 */
	@Column(name="BANNER_SUBJECT_EN", length = 1000)
	private String subjectEn;
	
	@Column(name = "BANNER_LINK_URL", length = 1000)
	private String linkUrl;
	
	@Column(name="BANNER_CONTENT")
	private String content;
	
	/** 영문 설명. 비우면 화면에서 한글이 나간다 */
	@Column(name="BANNER_CONTENT_EN", length = 1000)
	private String contentEn;
	
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
	
	@Column(name = "banner_index", nullable = false)
	private Integer bannerIndex = 0;

	/** 노출 시작일. 비우면 제한 없음 */
	@Column(name = "BANNER_START_DATE")
	private java.time.LocalDate startDate;

	/** 노출 종료일. 비우면 제한 없음 */
	@Column(name = "BANNER_END_DATE")
	private java.time.LocalDate endDate;

	/** 켜기 / 끄기. 끄면 메인에 안 나간다 */
	@Column(name = "BANNER_USE_YN")
	private Boolean useYn = Boolean.TRUE;

	/**
	 * 오늘 메인에 내보낼 배너인지.
	 *
	 * 옛 줄은 useYn 이 비어 있을 수 있다. 그때는 켜진 것으로 본다.
	 * 기능을 새로 넣었다고 해서 이미 돌고 있던 배너가 사라지면 안 된다.
	 */
	public boolean isVisibleOn(java.time.LocalDate today) {
		if (Boolean.FALSE.equals(useYn)) {
			return false;
		}
		if (startDate != null && today.isBefore(startDate)) {
			return false;
		}
		if (endDate != null && today.isAfter(endDate)) {
			return false;
		}
		return true;
	}

	/** 지금 켜져 있는지. 화면에서 스위치를 그릴 때 쓴다 */
	public boolean isOn() {
		return !Boolean.FALSE.equals(useYn);
	}

	/** 영문 제목이 아직 없는지 */
	public boolean isMissingSubjectEn() {
		return subjectEn == null || subjectEn.isBlank();
	}

	/** 영문 내용이 아직 없는지 */
	public boolean isMissingContentEn() {
		return contentEn == null || contentEn.isBlank();
	}
}










