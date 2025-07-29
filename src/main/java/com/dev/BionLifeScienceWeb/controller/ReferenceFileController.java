package com.dev.BionLifeScienceWeb.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.ReferenceFile;
import com.dev.BionLifeScienceWeb.repository.ReferenceFileRepository;
import com.dev.BionLifeScienceWeb.service.ReferenceFileService;

@Controller
@RequestMapping("/admin")
public class ReferenceFileController {

	@Autowired
	ReferenceFileService referenceFileService;
	
	@Autowired
	ReferenceFileRepository referenceFileRepository;
	
	@RequestMapping("/referenceManager")
	public String referenceManager(
			Model model
			) {
		model.addAttribute("files", referenceFileRepository.findAll());
		return "admin/referenceManager";
	}
	
	
	@RequestMapping("/referenceFileInsert")
	@ResponseBody
	public String referenceFileInsert(
			MultipartFile file,
			ReferenceFile f,
			Model model
			) throws IllegalStateException, IOException {
		
		referenceFileService.referenceFileInsert(file, f);
		StringBuffer sb = new StringBuffer();
		String msg = "파일이 등록 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/referenceManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	@RequestMapping("/deleteReferenceFile/{id}")
	@ResponseBody
	public String deleteReferenceFile(
			Model model,
			@PathVariable Long id	
			) {
		ReferenceFile rf = referenceFileRepository.findById(id).get();
		File file = new File(rf.getFileroad());
		referenceFileRepository.deleteById(id);
		StringBuffer sb = new StringBuffer();
		String msg = "파일이 삭제 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/referenceManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	
}
