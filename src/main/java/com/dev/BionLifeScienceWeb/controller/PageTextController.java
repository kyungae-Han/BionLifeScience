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

	@GetMapping("/pageTextManager")
	public String manager(Model model) {
		List<PageText> rows = pageTextService.listAll();

		// 화면·구역별로 묶어 보여 준다. 순서는 sort_index 를 따라간다.
		// 화면 코드를 앞에 붙여야 다른 화면의 같은 이름 구역과 섞이지 않는다
		Map<String, List<PageText>> sections = new LinkedHashMap<>();
		for (PageText row : rows) {
			String name = (row.getSection() == null || row.getSection().isBlank()) ? "기타" : row.getSection();
			sections.computeIfAbsent(row.getPageCode() + " · " + name, key -> new ArrayList<>()).add(row);
		}

		model.addAttribute("sections", sections);
		model.addAttribute("total", rows.size());
		return "admin/pageTextManager";
	}

	@PostMapping("/pageTextManager")
	public String save(@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
		int changed = pageTextService.save(params);
		redirectAttributes.addFlashAttribute("message", changed + "건의 문구를 저장했습니다.");
		return "redirect:/admin/pageTextManager";
	}
}
