package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
		model.addAttribute("certifications", certificationRepository.findAll());
		return "admin/certificationManager";
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
