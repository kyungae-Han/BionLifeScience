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

import lombok.Getter;
import lombok.Setter;

/**
 * 팝업 안에서 롤링되는 이미지 한 장.
 *
 * 한 줄에 한글용과 영문용이 같이 들어간다. 영문을 안 올리면 영문 화면에서도
 * 한글 이미지가 그대로 나간다 (EnText.pick 과 같은 규칙).
 */
@Entity
@Table(name = "popup_image")
@Getter
@Setter
public class PopupImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "POPUP_IMAGE_ID")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "POPUP_ID", nullable = false)
	private Popup popup;

	/** 롤링 순서 */
	@Column(name = "POPUP_IMAGE_SORT_INDEX")
	private Integer sortIndex = 0;

	// ── 한글 이미지 ──────────────────────────────────────────────
	/** 올린 사람이 준 원래 파일 이름 */
	@Column(name = "POPUP_IMAGE_NAME", length = 500)
	private String name;

	/** 서버에 실제로 저장한 자리 */
	@Column(name = "POPUP_IMAGE_PATH", length = 1000)
	private String path;

	/** 브라우저가 부르는 주소 */
	@Column(name = "POPUP_IMAGE_ROAD", length = 1000)
	private String road;

	// ── 영문 이미지 ──────────────────────────────────────────────
	@Column(name = "POPUP_IMAGE_NAME_EN", length = 500)
	private String nameEn;

	@Column(name = "POPUP_IMAGE_PATH_EN", length = 1000)
	private String pathEn;

	@Column(name = "POPUP_IMAGE_ROAD_EN", length = 1000)
	private String roadEn;

	/** 이 줄에 영문 이미지가 아직 없는지. 관리 화면에서 표시하는 데 쓴다 */
	public boolean isMissingEn() {
		return roadEn == null || roadEn.isBlank();
	}
}
