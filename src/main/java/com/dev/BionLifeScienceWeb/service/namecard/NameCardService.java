package com.dev.BionLifeScienceWeb.service.namecard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev.BionLifeScienceWeb.model.namecard.NameCard;
import com.dev.BionLifeScienceWeb.repository.namecard.NameCardRepository;

import lombok.RequiredArgsConstructor;

/**
 * 명함 조회와 vCard(.vcf) 생성.
 *
 * vCard 는 파일로 두지 않고 매번 만든다. 관리자에서 연락처를 고치면
 * 저장 버튼이 내려주는 파일도 같이 바뀌어야 하기 때문이다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NameCardService {

	private static final DateTimeFormatter REV_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private final NameCardRepository nameCardRepository;

	/** 노출 중인 명함만 찾는다. 사용안함으로 돌린 명함은 없는 주소가 된다 */
	public Optional<NameCard> findVisible(String slug) {
		if (slug == null || slug.isBlank()) {
			return Optional.empty();
		}
		return nameCardRepository.findBySlugAndUseYn(slug.trim().toLowerCase(), "Y");
	}

	public String toVCard(NameCard card, boolean english) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("BEGIN:VCARD\r\n");
		sb.append("VERSION:3.0\r\n");

		String fullName = english ? nvl(card.getNameEn(), card.getNameKo()) : card.getNameKo();
		sb.append("N:").append(structuredName(fullName, english)).append("\r\n");
		sb.append("FN:").append(escape(fullName)).append("\r\n");

		String company = english ? nvl(card.getCompanyEn(), card.getCompanyKo()) : card.getCompanyKo();
		String dept = english ? nvl(card.getDeptEn(), card.getDeptKo()) : card.getDeptKo();
		if (notBlank(company) || notBlank(dept)) {
			// ORG 는 ";" 로 회사와 부서를 나눈다. 값 안의 ";" 는 escape() 가 막아준다.
			sb.append("ORG:").append(escape(company));
			if (notBlank(dept)) {
				sb.append(';').append(escape(dept));
			}
			sb.append("\r\n");
		}

		String title = english ? nvl(card.getTitleEn(), card.getTitleKo()) : card.getTitleKo();
		if (notBlank(title)) {
			sb.append("TITLE:").append(escape(title)).append("\r\n");
		}

		if (notBlank(card.getMobile())) {
			sb.append("TEL;TYPE=CELL,VOICE:").append(escape(card.getMobileIntl())).append("\r\n");
		}
		if (notBlank(card.getTel())) {
			sb.append("TEL;TYPE=WORK,VOICE:").append(escape(card.getTelIntl())).append("\r\n");
		}
		if (notBlank(card.getFax())) {
			sb.append("TEL;TYPE=WORK,FAX:").append(escape(card.getFaxIntl())).append("\r\n");
		}
		if (notBlank(card.getEmail())) {
			sb.append("EMAIL;TYPE=INTERNET,WORK:").append(escape(card.getEmail())).append("\r\n");
		}

		List<String> addressLines = english ? card.getAddressEnLines() : card.getAddressKoLines();
		if (!addressLines.isEmpty()) {
			sb.append("ADR;TYPE=WORK:").append(structuredAddress(addressLines)).append("\r\n");
		}

		if (notBlank(card.getWebsiteUrl())) {
			sb.append("URL:").append(escape(card.getWebsiteUrl())).append("\r\n");
		}
		if (notBlank(card.getCatalogUrl())) {
			sb.append("URL:").append(escape(card.getCatalogUrl())).append("\r\n");
		}

		LocalDateTime rev = card.getUpdatedAt() != null ? card.getUpdatedAt() : LocalDateTime.now();
		sb.append("REV:").append(rev.format(REV_FORMAT)).append("\r\n");
		sb.append("END:VCARD\r\n");
		return sb.toString();
	}

	/** 저장 버튼이 내려줄 파일 이름. dh_jang → dh-jang.vcf */
	public String vCardFileName(NameCard card) {
		return card.getSlug().replace('_', '-') + ".vcf";
	}

	/**
	 * N 은 "성;이름;;;" 순서다. 한글 이름은 첫 글자를 성으로,
	 * 영문 이름은 마지막 낱말을 성으로 본다.
	 */
	private String structuredName(String fullName, boolean english) {
		if (!notBlank(fullName)) {
			return ";;;;";
		}
		String name = fullName.trim();
		String family;
		String given;
		if (english) {
			int lastSpace = name.lastIndexOf(' ');
			family = lastSpace > 0 ? name.substring(lastSpace + 1) : name;
			given = lastSpace > 0 ? name.substring(0, lastSpace) : "";
		} else {
			family = name.substring(0, 1);
			given = name.substring(1);
		}
		return escape(family) + ";" + escape(given) + ";;;";
	}

	/**
	 * ADR 은 7칸이다: 사서함;추가;도로명;시;도;우편번호;국가.
	 * 주소를 칸별로 쪼개 받지 않으므로 도로명 칸에 한 줄로 넣는다.
	 * 앞에 우편번호가 붙어 있으면 그것만 제 칸으로 옮긴다.
	 */
	private String structuredAddress(List<String> lines) {
		String joined = String.join(" ", lines).trim();
		String postal = "";
		if (joined.matches("^\\d{5}\\s.*")) {
			postal = joined.substring(0, 5);
			joined = joined.substring(5).trim();
		}
		return ";;" + escape(joined) + ";;;" + escape(postal) + ";";
	}

	/** vCard 3.0 에서 값 안의 \ , ; 와 줄바꿈은 그대로 두면 칸 구분이 깨진다 */
	private String escape(String value) {
		if (value == null) {
			return "";
		}
		StringBuilder out = new StringBuilder(value.length() + 8);
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '\\', ';', ',' -> out.append('\\').append(c);
				case '\r' -> { /* 줄바꿈은 \n 하나로 맞춘다 */ }
				case '\n' -> out.append('\\').append('n');
				default -> out.append(c);
			}
		}
		return out.toString();
	}

	private static boolean notBlank(String value) {
		return value != null && !value.isBlank();
	}

	private static String nvl(String value, String fallback) {
		return notBlank(value) ? value : fallback;
	}
}
