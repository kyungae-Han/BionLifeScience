package com.dev.BionLifeScienceWeb.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 모든 VIEW 에서 정규 URL(canonical) 사용을 위한 도움 클래스.
 * ${canonicalUrl} 통해 접근하며 head 프래그먼트의 canonical / og:url 에 쓰인다.
 *
 * 쿼리스트링은 제외한다. ?page= / ?sort= 는 robots.txt 에서 이미 색인 차단 대상이고,
 * 정규 URL 은 파라미터 없는 형태여야 중복 색인이 생기지 않는다.
 */
@ControllerAdvice
public class SeoModelAttributeAdvice {

	@Value("${app.base-url:https://bionlifescience.com}")
	private String baseUrl;

	/** 네이버 서치어드바이저 소유확인 코드. application.yml 의 app.verification.naver */
	@Value("${app.verification.naver:}")
	private String naverVerification;

	/** 구글 서치콘솔 소유확인 코드. application.yml 의 app.verification.google */
	@Value("${app.verification.google:}")
	private String googleVerification;

	/** 절대 주소가 필요한 곳(og:image 등)에서 쓴다 */
	@ModelAttribute("baseUrl")
	public String baseUrl() {
		return baseUrl;
	}

	@ModelAttribute("naverVerification")
	public String naverVerification() {
		return naverVerification;
	}

	@ModelAttribute("googleVerification")
	public String googleVerification() {
		return googleVerification;
	}

	/**
	 * 언어 전환 링크의 앞부분. 보던 화면과 검색조건을 그대로 두고 lang 만 바꾸기 위한 것이다.
	 * "/brandList?page=2&" 처럼 항상 파라미터를 이어 붙일 수 있는 모양으로 돌려준다.
	 */
	@ModelAttribute("langSwitchBase")
	public String langSwitchBase(HttpServletRequest request) {
		String uri = request.getRequestURI();
		if (uri == null || uri.isEmpty()) {
			uri = "/";
		}

		String query = request.getQueryString();
		if (query == null || query.isBlank()) {
			return uri + "?";
		}

		// 이미 붙어 있는 lang 은 빼고 다시 붙인다. 두 번 붙으면 앞의 값이 이긴다.
		StringBuilder kept = new StringBuilder();
		for (String pair : query.split("&")) {
			if (pair.isEmpty() || pair.equals("lang") || pair.startsWith("lang=")) {
				continue;
			}
			if (kept.length() > 0) {
				kept.append('&');
			}
			kept.append(pair);
		}
		return kept.length() == 0 ? uri + "?" : uri + "?" + kept + "&";
	}

	@ModelAttribute("canonicalUrl")
	public String canonicalUrl(HttpServletRequest request) {
		String uri = request.getRequestURI();
		if (uri == null || uri.isEmpty()) {
			uri = "/";
		}
		// "/index" 와 "/" 는 같은 문서다 — 홈으로 통일한다
		if ("/index".equals(uri)) {
			uri = "/";
		}
		return baseUrl + uri;
	}
}
