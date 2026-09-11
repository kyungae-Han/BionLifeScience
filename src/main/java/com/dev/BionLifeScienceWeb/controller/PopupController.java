package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dev.BionLifeScienceWeb.model.Popup;
import com.dev.BionLifeScienceWeb.service.PopupService;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 &gt; 팝업 관리.
 * 왼쪽에 등록된 팝업 목록, 오른쪽에 등록·수정 칸이다.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class PopupController {

	private final PopupService popupService;

	@GetMapping("/popupManager")
	public String manager(@RequestParam(value = "id", required = false) Long id, Model model) {
		model.addAttribute("popups", popupService.list());
		model.addAttribute("popup", id == null ? new Popup() : popupService.find(id).orElseGet(Popup::new));
		model.addAttribute("anchors", Popup.Anchor.values());
		model.addAttribute("units", Popup.Unit.values());
		return "admin/popupManager";
	}

	@PostMapping("/popupSave")
	public String save(@ModelAttribute Popup popup,
			@RequestParam(value = "koFile", required = false) List<MultipartFile> koFiles,
			@RequestParam(value = "enFile", required = false) List<MultipartFile> enFiles,
			RedirectAttributes redirectAttributes) throws IllegalStateException, IOException {

		Long id = popupService.save(popup, koFiles, enFiles);
		redirectAttributes.addFlashAttribute("message", "팝업을 저장했습니다.");
		return "redirect:/admin/popupManager?id=" + id;
	}

	@PostMapping("/popupUse/{id}")
	public String use(@PathVariable Long id,
			@RequestParam("on") boolean on,
			RedirectAttributes redirectAttributes) {

		if (on) {
			popupService.turnOn(id);
			redirectAttributes.addFlashAttribute("message", "팝업을 켰습니다. 다른 팝업은 꺼집니다.");
		} else {
			popupService.turnOff(id);
			redirectAttributes.addFlashAttribute("message", "팝업을 껐습니다.");
		}
		return "redirect:/admin/popupManager?id=" + id;
	}

	@PostMapping("/popupDelete/{id}")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		popupService.delete(id);
		redirectAttributes.addFlashAttribute("message", "팝업을 삭제했습니다.");
		return "redirect:/admin/popupManager";
	}

	@PostMapping("/popupImageDelete/{imageId}")
	public String deleteImage(@PathVariable Long imageId,
			@RequestParam("popupId") Long popupId,
			RedirectAttributes redirectAttributes) {

		popupService.deleteImage(imageId);
		redirectAttributes.addFlashAttribute("message", "이미지를 삭제했습니다.");
		return "redirect:/admin/popupManager?id=" + popupId;
	}
}
