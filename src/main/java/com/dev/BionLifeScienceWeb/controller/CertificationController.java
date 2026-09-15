package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.Certification;
import com.dev.BionLifeScienceWeb.repository.CertificationRepository;
import com.dev.BionLifeScienceWeb.service.CertificationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class CertificationController {
	
	private final CertificationRepository certificationRepository;
	private final CertificationService certificationService;
	
	@GetMapping("/certificationManager")
	public String certificationManager(
			Model model
			) {
		model.addAttribute("certifications", certificationRepository.findAllByOrderByCertificationIndexAscIdAsc());
		return "admin/certificationManager";
	}
	
	/** 인증서 등록 화면. 목록과 분리해 두었다. 배너·브랜드·제품과 같은 방식이다 */
	@GetMapping("/certificationInsertForm")
	public String certificationInsertForm() {
		return "admin/certificationInsertForm";
	}

	@RequestMapping(value = "/certificationInsert",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public String bannerInsert(
			MultipartFile webFile,
			Certification certification,
			Model model
			) throws IllegalStateException, IOException {
		certificationService.certificationInsert(webFile, certification);
		StringBuffer sb = new StringBuffer();
		String msg = "인증서가 등록 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/certificationManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	/** 인증서 수정 화면. 등록 화면과 같은 모양으로 따로 둔다 */
	@GetMapping("/certificationUpdateForm/{id}")
	public String certificationUpdateForm(
			@PathVariable Long id,
			Model model
			) {
		Certification certification = certificationRepository.findById(id).orElse(null);
		if (certification == null) {
			return "redirect:/admin/certificationManager";
		}
		model.addAttribute("certification", certification);
		return "admin/certificationUpdateForm";
	}
	
	@RequestMapping(value = "/certificationUpdate",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public String certificationUpdate(
			MultipartFile webFile,
			Certification certification,
			Model model
			) throws IllegalStateException, IOException {
		certificationService.certificationUpdate(webFile, certification);
		StringBuffer sb = new StringBuffer();
		String msg = "인증서가 수정 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/certificationManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	/**
	 * 목록에서 끌어 놓은 순서를 저장한다. 배너의 bannerOrder 와 같은 방식이다.
	 * 넘어온 순서대로 1 부터 다시 매긴다.
	 */
	@PostMapping("/certificationOrder")
	@ResponseBody
	public String certificationOrder(@RequestParam("ids") List<Long> ids) {
		int index = 1;
		for (Long id : ids) {
			Certification certification = certificationRepository.findById(id).orElse(null);
			if (certification != null) {
				certification.setCertificationIndex(index++);
				certificationRepository.save(certification);
			}
		}
		return "ok";
	}
	
	@RequestMapping(value = "/deleteCertification/{id}",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public String deleteBanner(
			@PathVariable Long id,
			Model model
			) {
		
		certificationRepository.deleteById(id);
		StringBuffer sb = new StringBuffer();
		String msg = "인증서가 삭제 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/certificationManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
}
