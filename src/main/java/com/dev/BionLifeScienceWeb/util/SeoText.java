package com.dev.BionLifeScienceWeb.util;

/**
 * meta description 용 텍스트 가공.
 *
 * 제품/공지 본문은 에디터가 만든 HTML 이라 그대로 쓰면 태그가 검색결과에 노출된다.
 * 태그를 걷어내고 검색결과 스니펫 길이(약 155자)로 자른다.
 */
public final class SeoText {

	/** 네이버·구글 검색결과에 노출되는 설명 길이. 넘으면 뒤가 잘려서 의미가 끊긴다. */
	private static final int MAX_LENGTH = 155;

	private SeoText() {}

	/** HTML 본문 → 설명 문구. 내용이 없으면 fallback 을 쓴다. */
	public static String description(String html, String fallback) {
		String text = plain(html);
		return text.isEmpty() ? fallback : truncate(text);
	}

	/** 제목 + 본문을 이어 붙인 설명. 본문이 비어도 제목만으로 문장이 된다. */
	public static String description(String title, String html, String fallback) {
		String head = plain(title);
		String body = plain(html);

		if (head.isEmpty() && body.isEmpty()) return fallback;
		if (body.isEmpty()) return truncate(head);
		if (head.isEmpty()) return truncate(body);
		return truncate(head + " - " + body);
	}

	private static String plain(String html) {
		if (html == null) return "";
		return html.replaceAll("(?is)<(script|style).*?</\\1>", " ")
		           .replaceAll("<[^>]*>", " ")
		           .replace("&nbsp;", " ")
		           .replace("&amp;", "&")
		           .replace("&lt;", "<")
		           .replace("&gt;", ">")
		           .replace("&quot;", "\"")
		           .replaceAll("\\s+", " ")
		           .trim();
	}

	private static String truncate(String text) {
		if (text.length() <= MAX_LENGTH) return text;
		// 단어 중간에서 끊지 않도록 마지막 공백까지만 쓴다
		String cut = text.substring(0, MAX_LENGTH);
		int lastSpace = cut.lastIndexOf(' ');
		if (lastSpace > MAX_LENGTH - 30) cut = cut.substring(0, lastSpace);
		return cut.trim() + "...";
	}
}
