package com.dev.BionLifeScienceWeb.service.namecard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.namecard.NameCard;
import com.dev.BionLifeScienceWeb.model.namecard.NameCardDocument;
import com.dev.BionLifeScienceWeb.model.namecard.NameCardDocumentType;
import com.dev.BionLifeScienceWeb.repository.namecard.NameCardDocumentRepository;
import com.dev.BionLifeScienceWeb.repository.namecard.NameCardRepository;
import com.dev.BionLifeScienceWeb.repository.page.PageContentRepository;
import com.dev.BionLifeScienceWeb.repository.page.PageGroupRepository;

import lombok.RequiredArgsConstructor;

/**
 * 관리자에서 명함을 등록·수정·삭제한다.
 *
 * 여기서 지키는 두 가지가 있다.
 * 1) 명함 주소(slug)는 사이트가 이미 쓰는 주소를 가로채면 안 된다.
 * 2) 거래 서류는 아무 파일이나 올라가면 안 된다. 명함은 링크만 있으면 누구나 여는 공개 페이지다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NameCardAdminService {

	private static final String UPLOAD_DIR = "namecardfile";
	private static final String UPLOAD_URL_PREFIX = "/upload/" + UPLOAD_DIR;

	/** 거래 서류는 웹으로 바로 열리지 않는 곳에 둔다. 컨트롤러를 거쳐야만 내려간다 */
	private static final String DOC_DIR = "bion-namecard-doc";

	private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
	private static final long MAX_DOC_SIZE = 10L * 1024 * 1024;

	/**
	 * 사이트가 이미 쓰고 있는 최상위 주소. 명함 주소로 쓰면 명함이 열리지 않는다.
	 * (같은 주소가 둘이면 글자로 박힌 주소가 이긴다)
	 */
	private static final Set<String> RESERVED_SLUGS = Set.of(
			"about", "address", "admin", "administration", "api", "brandlist", "brandproductdetail",
			"brandproductsorted", "card", "certifications", "clientdelete", "clientinsert", "contact",
			"contact-body", "css", "error", "favicon", "front", "history", "images", "index", "join",
			"js", "login", "logout", "memberlogin", "memberloginform", "namecard", "noticedetail",
			"noticelist", "privacy", "privacy-policy", "productoverall", "references", "rnd", "robots",
			"searchall", "searchsorted", "session-timeout", "sitemap", "static", "upload");

	/**
	 * 파일 이름에 이 낱말이 들어 있으면 올릴 수 없다.
	 * 통장사본·재무자료처럼 공개되면 회사와 개인에게 바로 피해가 가는 서류를 막는다.
	 */
	private static final List<String> BLOCKED_DOC_KEYWORDS = List.of(
			"통장", "계좌", "예금", "잔고", "이체", "인감", "재무", "결산", "손익",
			"자산", "급여", "연봉", "세금계산서", "주민등록", "여권", "신분증", "가족관계", "등본",
			"bank", "bankbook", "account", "passbook", "balance", "payroll", "salary", "asset",
			"financial", "seal", "passport", "resident");

	@Value("${spring.upload.path}")
	private String commonPath;

	private final NameCardRepository nameCardRepository;
	private final NameCardDocumentRepository nameCardDocumentRepository;
	private final PageContentRepository pageContentRepository;
	private final PageGroupRepository pageGroupRepository;

	public List<NameCard> getList() {
		return nameCardRepository.findAllByOrderByNameCardIdDesc();
	}

	public NameCard getDetail(Long nameCardId) {
		return nameCardRepository.findById(nameCardId)
				.orElseThrow(() -> new IllegalArgumentException("명함 정보가 없습니다."));
	}

	public List<NameCardDocumentType> getDocumentTypes() {
		return List.of(NameCardDocumentType.values());
	}

	@Transactional
	public Long save(NameCard form, MultipartFile photoFile, MultipartFile ogFile) {
		String slug = validateSlug(form);

		NameCard entity = form.getNameCardId() != null ? getDetail(form.getNameCardId()) : new NameCard();

		entity.setSlug(slug);
		entity.setUseYn(yn(form.getUseYn()));
		entity.setNameKo(required(form.getNameKo(), "이름(한글)을 입력해주세요."));
		entity.setNameEn(trim(form.getNameEn()));
		entity.setTitleKo(trim(form.getTitleKo()));
		entity.setTitleEn(trim(form.getTitleEn()));
		entity.setDeptKo(trim(form.getDeptKo()));
		entity.setDeptEn(trim(form.getDeptEn()));
		entity.setCompanyKo(trim(form.getCompanyKo()));
		entity.setCompanyEn(trim(form.getCompanyEn()));
		// 명함 아래 "전화·문자·메일·지도" 네 칸은 디자인상 항상 네 개다.
		// 값이 비면 눌러도 아무 일이 없는 버튼이 남으므로 여기서 막는다.
		entity.setMobile(required(form.getMobile(), "휴대전화를 입력해주세요."));
		entity.setTel(trim(form.getTel()));
		entity.setFax(trim(form.getFax()));
		entity.setEmail(required(form.getEmail(), "이메일을 입력해주세요."));
		entity.setAddressKo(required(form.getAddressKo(), "주소(한글)를 입력해주세요."));
		entity.setAddressEn(trim(form.getAddressEn()));
		entity.setMapQueryKo(trim(form.getMapQueryKo()));
		entity.setMapQueryEn(trim(form.getMapQueryEn()));
		entity.setWebsiteUrl(validateHttpUrl(form.getWebsiteUrl(), "홈페이지 주소"));
		entity.setCatalogUrl(validateHttpUrl(form.getCatalogUrl(), "카탈로그 주소"));
		entity.setCatalogNoteKo(trim(form.getCatalogNoteKo()));
		entity.setCatalogNoteEn(trim(form.getCatalogNoteEn()));
		entity.setOgDescKo(emptyToNull(form.getOgDescKo()));
		entity.setOgDescEn(emptyToNull(form.getOgDescEn()));
		entity.setBizNo(trim(form.getBizNo()));
		entity.setShowDocsYn(yn(form.getShowDocsYn()));
		entity.setShowQrYn(yn(form.getShowQrYn()));
		entity.setQrUrl(validateHttpUrl(form.getQrUrl(), "QR 주소"));
		entity.setQrCaptionKo(trim(form.getQrCaptionKo()));
		entity.setQrCaptionEn(trim(form.getQrCaptionEn()));

		if ("Y".equals(entity.getShowQrYn()) && isBlank(entity.getQrUrl())) {
			throw new IllegalArgumentException("QR 영역을 노출하려면 QR 주소를 입력해야 합니다.");
		}

		if (photoFile != null && !photoFile.isEmpty()) {
			uploadImage(entity, photoFile, "photo");
		}
		if (ogFile != null && !ogFile.isEmpty()) {
			uploadImage(entity, ogFile, "og");
		}

		return nameCardRepository.save(entity).getNameCardId();
	}

	@Transactional
	public void delete(Long nameCardId) {
		NameCard entity = getDetail(nameCardId);
		entity.getDocuments().forEach(doc -> deleteFileQuietly(doc.getFilePath()));
		nameCardRepository.delete(entity);
	}

	/* ── 거래 서류 ─────────────────────────────────────────────── */

	@Transactional
	public void addDocument(Long nameCardId, NameCardDocumentType docType, MultipartFile file) {
		NameCard card = getDetail(nameCardId);

		if (docType == null) {
			throw new IllegalArgumentException("서류 종류를 선택해주세요.");
		}
		validateDocumentFile(file);

		Path dirPath = docBasePath().resolve(card.getSlug());
		File dir = dirPath.toFile();
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IllegalStateException("서류 업로드 폴더 생성에 실패했습니다. path=" + dir.getAbsolutePath());
		}

		String savedName = UUID.randomUUID().toString().replace("-", "") + ".pdf";
		File dest = dirPath.resolve(savedName).toFile();
		try {
			file.transferTo(dest);
		} catch (IOException e) {
			throw new IllegalStateException("서류 저장에 실패했습니다. path=" + dest.getAbsolutePath(), e);
		}

		NameCardDocument document = new NameCardDocument();
		document.setNameCard(card);
		document.setDocType(docType);
		document.setFilePath(dest.getAbsolutePath().replace("\\", "/"));
		document.setFileName(file.getOriginalFilename());
		document.setFileSize(file.getSize());
		document.setSortIndex(card.getDocuments().size());
		card.getDocuments().add(document);

		nameCardDocumentRepository.save(document);
	}

	@Transactional
	public Long deleteDocument(Long nameCardDocumentId) {
		NameCardDocument document = nameCardDocumentRepository.findById(nameCardDocumentId)
				.orElseThrow(() -> new IllegalArgumentException("서류 정보가 없습니다."));
		Long nameCardId = document.getNameCard().getNameCardId();
		deleteFileQuietly(document.getFilePath());
		document.getNameCard().getDocuments().remove(document);
		nameCardDocumentRepository.delete(document);
		return nameCardId;
	}

	/**
	 * 명함에 붙일 수 있는 서류인지 본다.
	 * 확장자와 파일명만 보면 이름만 바꾼 파일이 통과하므로 앞머리 바이트까지 확인한다.
	 */
	private void validateDocumentFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("서류 파일을 선택해주세요.");
		}
		if (file.getSize() > MAX_DOC_SIZE) {
			throw new IllegalArgumentException("서류는 10MB 이하만 올릴 수 있습니다.");
		}

		String originalName = file.getOriginalFilename();
		if (originalName == null || originalName.isBlank()) {
			throw new IllegalArgumentException("서류 파일명이 올바르지 않습니다.");
		}
		String lowerName = originalName.toLowerCase(Locale.KOREA);
		if (!lowerName.endsWith(".pdf")) {
			throw new IllegalArgumentException("서류는 PDF 파일만 올릴 수 있습니다.");
		}

		for (String keyword : BLOCKED_DOC_KEYWORDS) {
			if (lowerName.contains(keyword)) {
				throw new IllegalArgumentException(
						"'" + keyword + "' 이(가) 들어간 파일은 올릴 수 없습니다. "
						+ "명함은 링크를 아는 사람이면 누구나 여는 공개 페이지입니다. "
						+ "통장사본·재무자료·신분증 같은 서류는 메일로 따로 보내주세요.");
			}
		}

		byte[] head = new byte[5];
		try (InputStream in = file.getInputStream()) {
			int read = in.read(head);
			if (read < 5 || !"%PDF-".equals(new String(head, StandardCharsets.US_ASCII))) {
				throw new IllegalArgumentException("PDF 파일이 아닙니다. 확장자만 바꾼 파일은 올릴 수 없습니다.");
			}
		} catch (IOException e) {
			throw new IllegalStateException("서류 파일을 읽지 못했습니다.", e);
		}
	}

	/* ── 명함 주소(slug) ───────────────────────────────────────── */

	private String validateSlug(NameCard form) {
		String slug = trim(form.getSlug()).toLowerCase(Locale.KOREA);
		if (slug.isEmpty()) {
			throw new IllegalArgumentException("명함 주소(메일 아이디)를 입력해주세요.");
		}
		// 최상위 주소에는 점을 쓸 수 없다. 점이 들어가면 정적 파일 요청으로 잡혀 명함이 열리지 않는다.
		if (!slug.matches("^[a-z0-9_-]+$")) {
			throw new IllegalArgumentException("명함 주소는 영문 소문자, 숫자, -, _ 만 쓸 수 있습니다. "
					+ "메일 아이디에 점이 있으면 점을 빼거나 _ 로 바꿔 주세요.");
		}
		if (RESERVED_SLUGS.contains(slug)) {
			throw new IllegalArgumentException("'" + slug + "' 은(는) 사이트가 이미 쓰는 주소라 명함 주소로 쓸 수 없습니다.");
		}
		if (pageGroupRepository.findByBasePathAndUseYn(slug, "Y").isPresent()
				|| pageContentRepository.findByPageGroup_BasePathAndSlugAndUseYn(slug, slug, "Y").isPresent()) {
			throw new IllegalArgumentException("'" + slug + "' 은(는) 이벤트 페이지가 쓰는 주소입니다.");
		}

		nameCardRepository.findBySlug(slug).ifPresent(exist -> {
			if (!exist.getNameCardId().equals(form.getNameCardId())) {
				throw new IllegalArgumentException("이미 등록된 명함 주소입니다.");
			}
		});
		return slug;
	}

	/* ── 이미지 ────────────────────────────────────────────────── */

	private void uploadImage(NameCard entity, MultipartFile file, String kind) {
		String originalName = file.getOriginalFilename();
		if (originalName == null || originalName.isBlank()) {
			throw new IllegalArgumentException("이미지 파일명이 올바르지 않습니다.");
		}
		String lowerName = originalName.toLowerCase(Locale.KOREA);
		if (!(lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png"))) {
			throw new IllegalArgumentException("이미지는 jpg, jpeg, png 만 올릴 수 있습니다.");
		}
		if (file.getSize() > MAX_IMAGE_SIZE) {
			throw new IllegalArgumentException("이미지는 5MB 이하만 올릴 수 있습니다.");
		}

		String datePath = LocalDate.now().toString();
		Path dirPath = uploadBasePath().resolve(kind).resolve(entity.getSlug()).resolve(datePath);
		File dir = dirPath.toFile();
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IllegalStateException("이미지 업로드 폴더 생성에 실패했습니다. path=" + dir.getAbsolutePath());
		}

		String ext = originalName.substring(originalName.lastIndexOf("."));
		String savedName = UUID.randomUUID().toString().replace("-", "") + ext;
		File dest = dirPath.resolve(savedName).toFile();
		try {
			file.transferTo(dest);
		} catch (IOException e) {
			throw new IllegalStateException("이미지 저장에 실패했습니다. path=" + dest.getAbsolutePath(), e);
		}

		String road = UPLOAD_URL_PREFIX + "/" + kind + "/" + entity.getSlug() + "/" + datePath + "/" + savedName;
		if ("photo".equals(kind)) {
			entity.setPhotoName(savedName);
			entity.setPhotoPath(dest.getAbsolutePath().replace("\\", "/"));
			entity.setPhotoRoad(road);
		} else {
			entity.setOgName(savedName);
			entity.setOgPath(dest.getAbsolutePath().replace("\\", "/"));
			entity.setOgRoad(road);
		}
	}

	private Path uploadBasePath() {
		return Paths.get(commonPath).toAbsolutePath().normalize().resolve(UPLOAD_DIR);
	}

	/** 웹으로 열리는 폴더 바깥이다. 운영 기준으로 webapps 옆에 생긴다 */
	private Path docBasePath() {
		Path base = Paths.get(commonPath).toAbsolutePath().normalize();
		Path parent = base.getParent();
		return (parent != null ? parent : base).resolve(DOC_DIR);
	}

	private void deleteFileQuietly(String path) {
		if (path == null || path.isBlank()) {
			return;
		}
		File file = new File(path);
		if (file.exists() && !file.delete()) {
			file.deleteOnExit();
		}
	}

	/* ── 작은 도우미 ───────────────────────────────────────────── */

	private String validateHttpUrl(String value, String label) {
		String url = trim(value);
		if (url.isEmpty()) {
			return "";
		}
		if (!url.matches("^https?://\\S+$")) {
			throw new IllegalArgumentException(label + "는 http:// 또는 https:// 로 시작하는 주소로 입력해주세요.");
		}
		return url;
	}

	private String required(String value, String message) {
		String trimmed = trim(value);
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException(message);
		}
		return trimmed;
	}

	/** 체크박스는 켜면 "Y" 가 오고 끄면 아무것도 오지 않는다 */
	private String yn(String value) {
		return "Y".equalsIgnoreCase(trim(value)) ? "Y" : "N";
	}

	/** 빈 칸은 null 로 저장한다. 화면에서 "값 없음" 을 가려내기 위해서다 */
	private String emptyToNull(String value) {
		String trimmed = trim(value);
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String trim(String value) {
		return value == null ? "" : value.trim();
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
