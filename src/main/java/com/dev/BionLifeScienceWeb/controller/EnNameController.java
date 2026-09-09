package com.dev.BionLifeScienceWeb.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dev.BionLifeScienceWeb.service.brand.EnNameService;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 &gt; 영문명 관리.
 * 메뉴와 제품에 붙는 영문 이름을 한 화면에서 채운다.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class EnNameController {

	private final EnNameService enNameService;

	@GetMapping("/enNameManager")
	public String manager(Model model) {
		model.addAttribute("brands", enNameService.brands());
		model.addAttribute("bigSorts", enNameService.bigSorts());
		model.addAttribute("middleSorts", enNameService.middleSorts());
		model.addAttribute("smallSorts", enNameService.smallSorts());
		model.addAttribute("products", enNameService.products());
		return "admin/enNameManager";
	}

	@PostMapping("/enNameManager")
	public String save(@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
		int changed = enNameService.saveEnglishNames(params);
		redirectAttributes.addFlashAttribute("message", changed + "건의 영문명을 저장했습니다.");
		return "redirect:/admin/enNameManager";
	}
}
