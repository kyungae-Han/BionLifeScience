package com.dev.BionLifeScienceWeb.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dev.BionLifeScienceWeb.model.brand.Brand;
import com.dev.BionLifeScienceWeb.model.brand.BrandProduct;
import com.dev.BionLifeScienceWeb.model.Notice;
import com.dev.BionLifeScienceWeb.model.page.PageGroup;
import com.dev.BionLifeScienceWeb.repository.NoticeRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandProductRepository;
import com.dev.BionLifeScienceWeb.repository.brand.BrandRepository;
import com.dev.BionLifeScienceWeb.repository.page.PageGroupRepository;

import lombok.RequiredArgsConstructor;

/**
 * 검색엔진용 sitemap.xml 생성.
 *
 * 정적 URL 은 상수로, 나머지는 DB 에서 매 요청 시 조회한다.
 * DynamicPageController 의 "/{basePath:[^.]+}" 는 점(.)이 포함된 경로를 매칭하지 않고,
 * 명시적 매핑이 패턴 매핑보다 우선하므로 "/sitemap.xml" 은 여기로 들어온다.
 */
@Controller
@RequiredArgsConstructor
public class SitemapController {

	@Value("${app.base-url:https://bionlifescience.com}")
	private String baseUrl;

	private final BrandRepository brandRepository;
	private final BrandProductRepository brandProductRepository;
	private final NoticeRepository noticeRepository;
	private final PageGroupRepository pageGroupRepository;

	/** robots.txt 에서 색인 차단한 경로 — 사이트맵에도 넣으면 안 된다 (robots.txt 와 함께 관리) */
	private static final List<String> ROBOTS_DISALLOWED = List.of(
			"admin", "administration", "memberLoginForm", "test");

	/** 상시 노출되는 정적 페이지: 경로, 우선순위, 갱신주기 */
	private static final String[][] STATIC_PAGES = {
		{ "/",                "1.0", "daily"   },
		{ "/about",           "0.8", "monthly" },
		{ "/rnd",             "0.8", "monthly" },
		{ "/history",         "0.5", "yearly"  },
		{ "/certifications",  "0.6", "monthly" },
		{ "/address",         "0.4", "yearly"  },
		{ "/contact",         "0.5", "yearly"  },
		{ "/productOverall",  "0.8", "weekly"  },
		{ "/brandList",       "0.9", "weekly"  },
		{ "/noticeList",      "0.7", "weekly"  },
		{ "/references",      "0.6", "monthly" },
	};

	@GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE + ";charset=UTF-8")
	@ResponseBody
	@Transactional(readOnly = true)
	public String sitemap() {

		String today = LocalDate.now().toString();
		StringBuilder sb = new StringBuilder(16384);

		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
		  .append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

		for (String[] page : STATIC_PAGES) {
			appendUrl(sb, page[0], today, page[1], page[2]);
		}

		// 자사브랜드 상세 — 독립 HTML 페이지가 있는 유일한 브랜드 유형이고,
		// 노출 우선순위가 가장 높은 페이지다 (운영 기준 /brandList/own/59, /brandList/own/73).
		// 타사브랜드는 /brandList 안에서 AJAX(/brandList/{id}/products, JSON)로만 그려지므로
		// 색인 가능한 자체 URL 이 없다. 넣으면 JSON 이 색인된다.
		for (Brand brand : brandRepository.findAllByOrderByBrandIndexAsc()) {
			if (brand.getType() != Brand.BrandType.OWN) continue;
			appendUrl(sb, "/brandList/own/" + brand.getId(), today, "0.9", "weekly");
		}

		// 자사브랜드 소속 제품을 먼저, 높은 우선순위로 싣는다.
		// 타입으로 한 번에 조회해서 제품마다 getBrand() 를 타는 N+1 을 피한다.
		Set<Long> ownProductIds = new LinkedHashSet<>();
		for (BrandProduct p : brandProductRepository
				.findAllByBrand_TypeOrderByBrandProductIndexAsc(Brand.BrandType.OWN)) {
			ownProductIds.add(p.getId());
			appendUrl(sb, "/brandProductDetail/" + p.getId(), today, "0.9", "weekly");
		}

		// 나머지 취급제품
		for (BrandProduct p : brandProductRepository.findAllByOrderByBrandProductIndexAsc()) {
			if (ownProductIds.contains(p.getId())) continue;
			appendUrl(sb, "/brandProductDetail/" + p.getId(), today, "0.6", "monthly");
		}

		// 공지사항 상세 — 작성일을 lastmod 로 사용
		List<Notice> notices = noticeRepository.findAll();
		for (Notice notice : notices) {
			String lastmod = notice.getDate() == null ? today
					: notice.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
			appendUrl(sb, "/noticeDetail/" + notice.getId(), lastmod, "0.5", "monthly");
		}

		// 관리자에서 만든 동적 페이지 (DynamicPageController)
		for (PageGroup group : pageGroupRepository.findAll()) {
			String basePath = group.getBasePath();
			if (!"Y".equals(group.getUseYn()) || basePath == null || basePath.isBlank()) continue;
			if (ROBOTS_DISALLOWED.contains(basePath)) continue;
			appendUrl(sb, "/" + basePath, today, "0.6", "monthly");
		}

		sb.append("</urlset>\n");
		return sb.toString();
	}

	private void appendUrl(StringBuilder sb, String path, String lastmod, String priority, String changefreq) {
		sb.append("  <url>\n")
		  .append("    <loc>").append(escapeXml(baseUrl + path)).append("</loc>\n")
		  .append("    <lastmod>").append(lastmod).append("</lastmod>\n")
		  .append("    <changefreq>").append(changefreq).append("</changefreq>\n")
		  .append("    <priority>").append(priority).append("</priority>\n")
		  .append("  </url>\n");
	}

	private String escapeXml(String s) {
		return s.replace("&", "&amp;")
		        .replace("<", "&lt;")
		        .replace(">", "&gt;")
		        .replace("\"", "&quot;")
		        .replace("'", "&apos;");
	}
}
