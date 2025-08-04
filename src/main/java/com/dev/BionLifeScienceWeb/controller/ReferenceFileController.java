package com.dev.BionLifeScienceWeb.controller;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.ReferenceFile;
import com.dev.BionLifeScienceWeb.repository.ReferenceFileRepository;
import com.dev.BionLifeScienceWeb.service.ReferenceFileService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ReferenceFileController {

	private final ReferenceFileService referenceFileService;
	private final ReferenceFileRepository referenceFileRepository;
	
	@GetMapping("/referenceManager")
	public String referenceManager(
			Model model
			) {
		model.addAttribute("files", referenceFileRepository.findAll());
		return "admin/referenceManager";
	}
	
	
	@RequestMapping(value = "/referenceFileInsert",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
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
	
	@RequestMapping(value = "/deleteReferenceFile/{id}",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public String deleteReferenceFile(
			Model model,
			@PathVariable Long id	
			) {
		// 1. 파일 정보 조회
	    ReferenceFile rf = referenceFileRepository.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("파일 정보가 존재하지 않습니다."));

	    // 2. 실제 파일 삭제 (파일 경로는 getFilepath() 또는 getFileroad()에 따라 확인)
	    File file = new File(rf.getFilepath());
	    if (file.exists()) {
	        boolean deleted = file.delete();
	        if (!deleted) {
	            throw new IllegalStateException("파일 삭제에 실패하였습니다.");
	        }
	    }

	    // 3. DB에서 파일 정보 삭제
	    referenceFileRepository.deleteById(id);

	    // 4. 스크립트 반환
	    StringBuffer sb = new StringBuffer();
	    String msg = "파일이 삭제 되었습니다.";

	    sb.append("alert('" + msg + "');");
	    sb.append("location.href='/admin/referenceManager'");
	    sb.append("</script>");
	    sb.insert(0, "<script>");

	    return sb.toString();
	}
}
