package com.dev.BionLifeScienceWeb.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 관리자 왼쪽 차림표의 대카테고리.
 *
 * 일반회원(ROLE_USER)은 관리자가 체크해 준 대카테고리만 쓸 수 있다.
 * 대카테고리를 받으면 그 아래 하위 화면과 등록·수정·삭제 주소까지 전부 열린다.
 *
 * 주소는 전부 /admin/한칸 모양이라 앞 경로로는 카테고리를 가를 수 없다.
 * 그래서 /admin/ 바로 뒤 첫 칸(segment)을 여기 적어 둔 표로 찾는다.
 * 표에 없는 주소는 관리자만 들어간다. 새 화면을 만들면 반드시 여기에 첫 칸을 넣는다.
 */
public enum AdminMenu {

	CLIENT("client", "문의사항 관리", "/admin/clientManager",
			"clientManager", "clientDetail", "clientUpdate", "fileDownload"),

	HISTORY("history", "연혁 관리", "/admin/historyManager",
			"historyManager", "historySubjectInsert", "historyContentInsert", "historyDelete"),

	NOTICE("notice", "공지사항 관리", "/admin/noticeManager",
			"noticeManager", "noticeSubjectManager", "noticeSubjectInsert", "noticeSubjectDelete",
			"noticeUpdate", "noticeDetail", "noticeInsertForm", "noticeInsert", "noticeDelete",
			"notice"), // /admin/notice/image : 공지 편집기 이미지 올리기

	INFORMATION("information", "회사정보관리", "/admin/siteManager",
			"siteManager", "companyInfoInsert", "emailInsert", "deleteEmail", "tldInsert", "deleteTld",
			"allowedEmailInsert", "deleteAllowedEmail", "changeEmailStatus"),

	REFERENCE("reference", "자료실 관리", "/admin/referenceManager",
			"referenceManager", "referenceFileInsert", "deleteReferenceFile", "referenceUpdate"),

	BRAND("brand", "제품관리(브랜드관리)", "/admin/brandManager",
			"brandManager", "brandInsertForm", "brandInsert", "brandDelete", "brandDetail", "brandUpdate",
			"brandSortManager",
			"brandBigSortInsert", "brandBigSortSearch", "brandBigSortDelete",
			"brandMiddleSortInsert", "brandMiddleSortSearch", "brandMiddleSortDelete",
			"brandSmallSortInsert", "brandSmallSortSearch", "brandSmallSortDelete",
			"brandProductManager", "brandProductInsertForm", "brandProductInsert", "brandProductDetail",
			"brandProductUpdate", "brandProductDelete", "brandProductSpecDelete",
			"brandCenter"), // 상단 ProductCenter

	SITE("site", "사이트관리", "/admin/bannerManager",
			"bannerManager", "bannerInsertForm", "bannerInsert", "getBanner", "bannerUpdate", "deleteBanner",
			"bannerPeriod", "bannerUse", "bannerOrder",
			"eventManager", "eventInsert",
			"pageManager", "pageGroup",
			"popupManager", "popupSave", "popupUse", "popupDelete", "popupImageDelete"),

	CERTIFICATION("certification", "인증서 관리", "/admin/certificationManager",
			"certificationManager", "certificationInsertForm", "certificationInsert",
			"certificationUpdateForm", "certificationUpdate", "certificationOrder", "deleteCertification"),

	NAMECARD("namecard", "디지털 명함 관리", "/admin/namecardManager",
			"namecardManager"),

	EN_NAME("enName", "영문명 관리", "/admin/enNameManager",
			"enNameManager"),

	PAGE_TEXT("pageText", "페이지 문구 관리", "/admin/pageTextManager",
			"pageTextManager");

	private final String code;
	private final String label;
	private final String landing;
	private final Set<String> segments;

	AdminMenu(String code, String label, String landing, String... segments) {
		this.code = code;
		this.label = label;
		this.landing = landing;
		this.segments = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(segments)));
	}

	public String getCode() { return code; }
	public String getLabel() { return label; }
	public String getLanding() { return landing; }
	public Set<String> getSegments() { return segments; }

	private static final Map<String, AdminMenu> BY_SEGMENT = new HashMap<>();
	private static final Map<String, AdminMenu> BY_CODE = new HashMap<>();

	static {
		for (AdminMenu menu : values()) {
			BY_CODE.put(menu.code, menu);
			for (String segment : menu.segments) {
				AdminMenu dup = BY_SEGMENT.put(segment, menu);
				if (dup != null) {
					throw new IllegalStateException("주소 첫 칸이 두 카테고리에 겹친다: " + segment);
				}
			}
		}
	}

	/** 코드로 찾는다. 모르는 코드면 null */
	public static AdminMenu fromCode(String code) {
		return code == null ? null : BY_CODE.get(code);
	}

	/**
	 * 요청 주소가 속한 카테고리. /clientDelete 는 /admin 밖이지만 문의사항 삭제다.
	 * 표에 없으면 null 이고, 그 주소는 관리자만 들어간다.
	 */
	public static AdminMenu fromPath(String path) {
		if (path == null) {
			return null;
		}
		if (path.equals("/clientDelete")) {
			return CLIENT;
		}
		if (!path.startsWith("/admin/")) {
			return null;
		}
		String rest = path.substring("/admin/".length());
		int slash = rest.indexOf('/');
		String segment = slash < 0 ? rest : rest.substring(0, slash);
		return BY_SEGMENT.get(segment);
	}
}
