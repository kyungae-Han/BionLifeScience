package com.dev.BionLifeScienceWeb.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dev.BionLifeScienceWeb.model.page.PageText;
import com.dev.BionLifeScienceWeb.service.page.PageTextService;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 &gt; 페이지 문구 관리.
 * 화면에 박혀 있던 글을 한글·영문 두 칸으로 고친다.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class PageTextController {

	private final PageTextService pageTextService;

	/**
	 * 화면 코드에 붙일 사람이 읽을 이름.
	 * page_text 에는 이름 칸이 없다. 화면이 늘어나면 여기에 한 줄 더 넣는다.
	 * 없는 코드는 코드 그대로 보인다.
	 */
	private static final Map<String, String> PAGE_LABELS = Map.of(
			"index", "메인 화면",
			"rnd", "기업부설 연구소");

	@GetMapping("/pageTextManager")
	public String manager(Model model) {
		List<PageText> rows = pageTextService.listAll();

		// 화면 → 구역 → 문구 로 두 번 묶는다. 순서는 sort_index 를 따라간다.
		// 왼쪽 나무 차림표와 가운데 목록이 같은 자료를 쓴다
		Map<String, Map<String, List<PageText>>> pages = new LinkedHashMap<>();
		Map<String, Integer> pageCounts = new LinkedHashMap<>();
		int emptyEn = 0;

		for (PageText row : rows) {
			String code = row.getPageCode();
			String section = (row.getSection() == null || row.getSection().isBlank()) ? "기타" : row.getSection();

			pages.computeIfAbsent(code, key -> new LinkedHashMap<>())
					.computeIfAbsent(section, key -> new ArrayList<>())
					.add(row);
			pageCounts.merge(code, 1, Integer::sum);

			if (row.getTextEn() == null || row.getTextEn().isBlank()) {
				emptyEn++;
			}
		}

		Map<String, String> pageLabels = new LinkedHashMap<>();
		for (String code : pages.keySet()) {
			pageLabels.put(code, PAGE_LABELS.getOrDefault(code, code));
		}

		model.addAttribute("pages", pages);
		model.addAttribute("pageCounts", pageCounts);
		model.addAttribute("pageLabels", pageLabels);
		model.addAttribute("total", rows.size());
		model.addAttribute("emptyEn", emptyEn);
		return "admin/pageTextManager";
	}

	@PostMapping("/pageTextManager")
	public String save(@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
		int changed = pageTextService.save(params);
		redirectAttributes.addFlashAttribute("message", changed + "건의 문구를 저장했습니다.");
		return "redirect:/admin/pageTextManager";
	}
}
