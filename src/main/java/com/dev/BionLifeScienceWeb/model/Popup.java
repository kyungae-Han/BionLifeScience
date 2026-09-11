package com.dev.BionLifeScienceWeb.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

/**
 * 메인 화면에 뜨는 팝업 한 건.
 *
 * 창은 하나고 그 안에서 이미지가 롤링된다. 이미지는 {@link PopupImage} 에 줄 단위로 있다.
 * 켜져 있는(useYn = true) 팝업은 한 번에 하나다. 서비스에서 지켜 준다.
 */
@Entity
@Table(name = "popup")
@Getter
@Setter
public class Popup {

	/** 어디를 기준으로 좌표를 잡을지 */
	public enum Anchor {
		TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
	}

	/** 좌표 단위 */
	public enum Unit {
		PX, PERCENT
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "POPUP_ID")
	private Long id;

	/** 관리자만 보는 이름. 화면에는 안 나간다 */
	@Column(name = "POPUP_SUBJECT", length = 200, nullable = false)
	private String subject;

	/** 이미지를 눌렀을 때 갈 곳. 비우면 눌러도 아무 일 없다 */
	@Column(name = "POPUP_LINK_URL", length = 1000)
	private String linkUrl;

	/** 새 창으로 열지 */
	@Column(name = "POPUP_LINK_NEW_WINDOW")
	private Boolean linkNewWindow = Boolean.TRUE;

	/** 노출 기간. 비우면 제한 없음 */
	@Column(name = "POPUP_START_DATE")
	private LocalDate startDate;

	@Column(name = "POPUP_END_DATE")
	private LocalDate endDate;

	/** 켜기 / 끄기 */
	@Column(name = "POPUP_USE_YN", nullable = false)
	private Boolean useYn = Boolean.FALSE;

	@Column(name = "POPUP_ANCHOR", length = 20, nullable = false)
	private String anchor = Anchor.BOTTOM_RIGHT.name();

	/** 기준점에서 가로로 띄울 거리 */
	@Column(name = "POPUP_OFFSET_X")
	private Integer offsetX = 40;

	/** 기준점에서 세로로 띄울 거리 */
	@Column(name = "POPUP_OFFSET_Y")
	private Integer offsetY = 40;

	/** 거리 단위. PX 또는 PERCENT */
	@Column(name = "POPUP_OFFSET_UNIT", length = 10, nullable = false)
	private String offsetUnit = Unit.PX.name();

	/** 팝업 너비 (px). 높이는 이미지 비율을 따라간다 */
	@Column(name = "POPUP_WIDTH")
	private Integer width = 380;

	/** 이미지가 두 장 이상일 때 한 장을 보여 주는 시간 (밀리초). 0 이면 안 넘긴다 */
	@Column(name = "POPUP_ROLL_INTERVAL")
	private Integer rollInterval = 4000;

	/**
	 * 뜰 때 미끄러져 들어오는 시간 (밀리초). 0 이면 그냥 나타난다.
	 * 들어오는 방향은 뜨는 자리를 따라간다. 오른쪽이면 오른쪽에서, 가운데면 아래에서 올라온다.
	 */
	@Column(name = "POPUP_ANIM_SPEED")
	private Integer animSpeed = 450;

	@OneToMany(mappedBy = "popup", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortIndex asc, id asc")
	private List<PopupImage> images = new ArrayList<>();

	/** 지금 이 날짜에 보여 줄 팝업인지. 켜져 있고 기간 안에 들어와야 한다 */
	public boolean isVisibleOn(LocalDate today) {
		if (!Boolean.TRUE.equals(useYn)) {
			return false;
		}
		if (startDate != null && today.isBefore(startDate)) {
			return false;
		}
		if (endDate != null && today.isAfter(endDate)) {
			return false;
		}
		return !images.isEmpty();
	}

	/**
	 * 뜰 때 미끄러뜨릴지.
	 *
	 * 템플릿에서 animSpeed > 0 을 직접 쓰면 안 된다. 부등호가 속성값 안에 들어가면
	 * 타임리프 파서가 태그로 읽어서 화면 전체가 깨진다. 그래서 여기서 판단한다.
	 */
	public boolean isAnimated() {
		return animSpeed != null && animSpeed.intValue() > 0;
	}

	/** 속도 고르는 칸에서 지금 값과 같은지 볼 때 쓴다 */
	public boolean isSpeed(int ms) {
		int current = (animSpeed == null) ? 450 : animSpeed.intValue();
		return current == ms;
	}

	/** 화면에서 쓰는 CSS 기준점 이름 */
	public String getAnchorClass() {
		return anchor == null ? "bottom-right" : anchor.toLowerCase().replace('_', '-');
	}

	public String getUnitSuffix() {
		return Unit.PERCENT.name().equals(offsetUnit) ? "%" : "px";
	}
}
