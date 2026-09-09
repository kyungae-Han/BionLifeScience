package com.dev.BionLifeScienceWeb.controller;

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

import com.dev.BionLifeScienceWeb.model.namecard.NameCard;
import com.dev.BionLifeScienceWeb.model.namecard.NameCardDocumentType;
import com.dev.BionLifeScienceWeb.service.namecard.NameCardAdminService;

import lombok.RequiredArgsConstructor;

/**
 * 관리자 &gt; 디지털 명함 관리.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class NameCardAdminController {

	private final NameCardAdminService nameCardAdminService;

	@GetMapping("/namecardManager")
	public String list(Model model) {
		model.addAttribute("cardList", nameCardAdminService.getList());
		return "admin/namecard/list";
	}

	@GetMapping("/namecardManager/form")
	public String form(Model model) {
		NameCard card = new NameCard();
		card.setUseYn("Y");
		card.setShowDocsYn("N");
		card.setShowQrYn("N");

		model.addAttribute("card", card);
		model.addAttribute("docTypes", nameCardAdminService.getDocumentTypes());
		return "admin/namecard/write";
	}

	@GetMapping("/namecardManager/form/{id}")
	public String editForm(@PathVariable Long id, Model model) {
		model.addAttribute("card", nameCardAdminService.getDetail(id));
		model.addAttribute("docTypes", nameCardAdminService.getDocumentTypes());
		return "admin/namecard/write";
	}

	@PostMapping("/namecardManager")
	public String save(@ModelAttribute("card") NameCard card,
			@RequestParam(required = false) MultipartFile photoFile,
			@RequestParam(required = false) MultipartFile ogFile,
			RedirectAttributes redirectAttributes) {
		try {
			Long savedId = nameCardAdminService.save(card, photoFile, ogFile);
			redirectAttributes.addFlashAttribute("message", "저장되었습니다.");
			return "redirect:/admin/namecardManager/form/" + savedId;
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
			if (card.getNameCardId() != null) {
				return "redirect:/admin/namecardManager/form/" + card.getNameCardId();
			}
			return "redirect:/admin/namecardManager/form";
		}
	}

	@PostMapping("/namecardManager/delete")
	public String delete(@RequestParam Long id, RedirectAttributes redirectAttributes) {
		try {
			nameCardAdminService.delete(id);
			redirectAttributes.addFlashAttribute("message", "삭제되었습니다.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
		}
		return "redirect:/admin/namecardManager";
	}

	@PostMapping("/namecardManager/document")
	public String addDocument(@RequestParam Long nameCardId,
			@RequestParam(required = false) NameCardDocumentType docType,
			@RequestParam(required = false) MultipartFile docFile,
			RedirectAttributes redirectAttributes) {
		try {
			nameCardAdminService.addDocument(nameCardId, docType, docFile);
			redirectAttributes.addFlashAttribute("message", "서류가 등록되었습니다.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("message", e.getMessage());
		}
		return "redirect:/admin/namecardManager/form/" + nameCardId;
	}

	@PostMapping("/namecardManager/document/delete")
	public String deleteDocument(@RequestParam Long documentId, RedirectAttributes redirectAttributes) {
		Long nameCardId = nameCardAdminService.deleteDocument(documentId);
		redirectAttributes.addFlashAttribute("message", "서류가 삭제되었습니다.");
		return "redirect:/admin/namecardManager/form/" + nameCardId;
	}
}
