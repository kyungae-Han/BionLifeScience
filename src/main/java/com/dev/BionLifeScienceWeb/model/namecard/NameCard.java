package com.dev.BionLifeScienceWeb.model.namecard;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * 임직원 디지털 명함. 주소는 사내 메일 아이디를 그대로 쓴다 (dh_jang@... → /dh_jang).
 *
 * 한 장에 한글면·영문면이 같이 들어간다. 화면에서 한쪽만 감추는 방식이라
 * 두 언어의 값을 모두 들고 있어야 한다.
 */
@Entity
@Table(name = "name_card")
@Getter
@Setter
public class NameCard {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "name_card_id")
	private Long nameCardId;

	/** 명함 주소. 메일 아이디와 같다. 영문 소문자/숫자/-/_ 만 쓴다 */
	@Column(name = "slug", nullable = false, unique = true, length = 60)
	private String slug;

	@Column(name = "use_yn", nullable = false, length = 1)
	private String useYn = "Y";

	@Column(name = "name_ko", nullable = false, length = 100)
	private String nameKo;

	@Column(name = "name_en", length = 100)
	private String nameEn;

	@Column(name = "title_ko", length = 60)
	private String titleKo;

	@Column(name = "title_en", length = 100)
	private String titleEn;

	@Column(name = "dept_ko", length = 200)
	private String deptKo;

	@Column(name = "dept_en", length = 200)
	private String deptEn;

	@Column(name = "company_ko", length = 200)
	private String companyKo = "(주)바이온라이프사이언스";

	@Column(name = "company_en", length = 200)
	private String companyEn = "Bion Lifescience, Inc.";

	/** 국내 표기 그대로 입력한다. 국제 표기와 tel: 링크는 여기서 만들어 쓴다 */
	@Column(name = "mobile", length = 40)
	private String mobile;

	@Column(name = "tel", length = 40)
	private String tel;

	@Column(name = "fax", length = 40)
	private String fax;

	@Column(name = "email", length = 200)
	private String email;

	/** 줄바꿈으로 여러 줄을 넣는다. 화면에서 <br> 로 끊어 보여준다 */
	@Column(name = "address_ko", length = 500)
	private String addressKo;

	@Column(name = "address_en", length = 500)
	private String addressEn;

	/** 지도 버튼이 검색할 문구. 비우면 주소 첫 줄을 쓴다 */
	@Column(name = "map_query_ko", length = 300)
	private String mapQueryKo;

	@Column(name = "map_query_en", length = 300)
	private String mapQueryEn;

	@Column(name = "website_url", length = 300)
	private String websiteUrl;

	@Column(name = "catalog_url", length = 300)
	private String catalogUrl;

	@Column(name = "catalog_note_ko", length = 200)
	private String catalogNoteKo;

	@Column(name = "catalog_note_en", length = 200)
	private String catalogNoteEn;

	@Column(name = "photo_road", length = 255)
	private String photoRoad;

	@Column(name = "photo_path", length = 255)
	private String photoPath;

	@Column(name = "photo_name", length = 255)
	private String photoName;

	/** 카카오톡·메일 미리보기 그림. 비우면 회사 기본 이미지를 쓴다 */
	@Column(name = "og_road", length = 255)
	private String ogRoad;

	@Column(name = "og_path", length = 255)
	private String ogPath;

	@Column(name = "og_name", length = 255)
	private String ogName;

	/** 거래 서류 영역 노출 여부 */
	@Column(name = "show_docs_yn", nullable = false, length = 1)
	private String showDocsYn = "N";

	@Column(name = "biz_no", length = 40)
	private String bizNo;

	/** QR 영역 노출 여부 */
	@Column(name = "show_qr_yn", nullable = false, length = 1)
	private String showQrYn = "N";

	/** QR 이 가리킬 주소. 이 값으로 QR 그림을 그때그때 만든다 */
	@Column(name = "qr_url", length = 500)
	private String qrUrl;

	@Column(name = "qr_caption_ko", length = 300)
	private String qrCaptionKo;

	@Column(name = "qr_caption_en", length = 300)
	private String qrCaptionEn;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "nameCard", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortIndex asc, nameCardDocumentId asc")
	private List<NameCardDocument> documents = new ArrayList<>();

	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	/* ── 화면에서 바로 쓰는 파생 값. 저장하지 않는다 ────────────────── */

	@Transient
	public boolean isVisible() {
		return "Y".equals(useYn);
	}

	@Transient
	public boolean isDocsVisible() {
		return "Y".equals(showDocsYn) && !documents.isEmpty();
	}

	@Transient
	public boolean isQrVisible() {
		return "Y".equals(showQrYn) && qrUrl != null && !qrUrl.isBlank();
	}

	/** 010-5541-0988 → +82-10-5541-0988 */
	@Transient
	public String getMobileIntl() {
		return toIntl(mobile);
	}

	@Transient
	public String getTelIntl() {
		return toIntl(tel);
	}

	@Transient
	public String getFaxIntl() {
		return toIntl(fax);
	}

	/** 010-5541-0988 → +821055410988 (tel: 링크용) */
	@Transient
	public String getMobileHref() {
		return toHref(mobile);
	}

	@Transient
	public String getTelHref() {
		return toHref(tel);
	}

	@Transient
	public List<String> getAddressKoLines() {
		return toLines(addressKo);
	}

	@Transient
	public List<String> getAddressEnLines() {
		return toLines(addressEn);
	}

	@Transient
	public List<String> getQrCaptionKoLines() {
		return toLines(qrCaptionKo);
	}

	@Transient
	public List<String> getQrCaptionEnLines() {
		return toLines(qrCaptionEn);
	}

	/** https://www.bionlifescience.com → www.bionlifescience.com */
	@Transient
	public String getWebsiteLabel() {
		return toLabel(websiteUrl);
	}

	@Transient
	public String getCatalogLabel() {
		return toLabel(catalogUrl);
	}

	/** 지도 검색어. 따로 넣지 않았으면 주소 첫 줄을 쓴다 */
	@Transient
	public String getMapQueryKoOrAddress() {
		return firstNotBlank(mapQueryKo, firstLine(addressKo));
	}

	@Transient
	public String getMapQueryEnOrAddress() {
		return firstNotBlank(mapQueryEn, firstLine(addressEn));
	}

	/**
	 * "연락처 복사하기" 가 클립보드에 넣는 문구. 메신저에 붙여 넣기 좋게 한 덩어리로 만든다.
	 */
	@Transient
	public String getCopyTextKo() {
		StringBuilder sb = new StringBuilder();
		appendLine(sb, join(" / ", join(" ", nameKo, titleKo), deptKo));
		appendLine(sb, companyKo);
		appendLabelled(sb, "M. ", mobile);
		appendLabelled(sb, "T. ", tel);
		appendLabelled(sb, "F. ", fax);
		appendLabelled(sb, "E. ", email);
		appendLine(sb, String.join(" ", getAddressKoLines()));
		appendLine(sb, getWebsiteLabel());
		appendLine(sb, getCatalogLabel());
		return sb.toString().trim();
	}

	@Transient
	public String getCopyTextEn() {
		StringBuilder sb = new StringBuilder();
		appendLine(sb, join(" / ", nameEn, titleEn));
		appendLine(sb, deptEn);
		appendLine(sb, companyEn);
		appendLabelled(sb, "M. ", getMobileIntl());
		appendLabelled(sb, "T. ", getTelIntl());
		appendLabelled(sb, "F. ", getFaxIntl());
		appendLabelled(sb, "E. ", email);
		appendLine(sb, String.join(" ", getAddressEnLines()));
		appendLine(sb, getWebsiteLabel());
		appendLine(sb, getCatalogLabel());
		return sb.toString().trim();
	}

	private static void appendLine(StringBuilder sb, String value) {
		if (value != null && !value.isBlank()) {
			sb.append(value.trim()).append('\n');
		}
	}

	private static void appendLabelled(StringBuilder sb, String label, String value) {
		if (value != null && !value.isBlank()) {
			sb.append(label).append(value.trim()).append('\n');
		}
	}

	private static String join(String separator, String... values) {
		return Arrays.stream(values)
				.filter(v -> v != null && !v.isBlank())
				.map(String::trim)
				.reduce((a, b) -> a + separator + b)
				.orElse("");
	}

	private static String toIntl(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		String trimmed = value.trim();
		return trimmed.startsWith("0") ? "+82-" + trimmed.substring(1) : trimmed;
	}

	private static String toHref(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		String digits = value.replaceAll("[^0-9]", "");
		if (digits.startsWith("0")) {
			digits = digits.substring(1);
		}
		return "+82" + digits;
	}

	private static String toLabel(String url) {
		if (url == null || url.isBlank()) {
			return "";
		}
		String label = url.trim().replaceFirst("^https?://", "");
		return label.endsWith("/") ? label.substring(0, label.length() - 1) : label;
	}

	private static List<String> toLines(String value) {
		if (value == null || value.isBlank()) {
			return List.of();
		}
		return Arrays.stream(value.replace("\r\n", "\n").split("\n"))
				.map(String::trim)
				.filter(line -> !line.isEmpty())
				.toList();
	}

	private static String firstLine(String value) {
		List<String> lines = toLines(value);
		return lines.isEmpty() ? "" : lines.get(0);
	}

	private static String firstNotBlank(String first, String second) {
		return (first != null && !first.isBlank()) ? first.trim() : second;
	}
}
