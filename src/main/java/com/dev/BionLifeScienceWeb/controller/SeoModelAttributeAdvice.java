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
