package com.dev.BionLifeScienceWeb.util;

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * DB 에서 나오는 이름·설명을 보고 있는 언어에 맞춰 골라 준다.
 *
 * 화면에서 이렇게 쓴다.
 *   th:text="${@enText.pick(brand.name, brand.nameEn)}"
 *
 * 영문 값이 비어 있으면 한글을 그대로 돌려준다. 그래서 번역이 덜 채워져도
 * 화면에 빈칸이 생기지 않는다.
 */
@Component
public class EnText {

	public String pick(String ko, String en) {
		if (isEnglish() && en != null && !en.isBlank()) {
			return en;
		}
		// 빈 값은 null 로 돌려준다. 화면에서 ?: 로 대체 문구를 쓸 수 있다
		return (ko == null || ko.isBlank()) ? null : ko;
	}

	/** 지금 화면이 영문인지. 템플릿에서 분기할 때 쓴다 */
	public boolean isEnglish() {
		Locale locale = LocaleContextHolder.getLocale();
		return locale != null && "en".equals(locale.getLanguage());
	}
}
