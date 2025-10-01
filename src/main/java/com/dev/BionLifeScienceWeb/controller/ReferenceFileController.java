package com.dev.BionLifeScienceWeb.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
			@RequestParam(required = false) Long id,
			Model model
			) {
		
		model.addAttribute("files", referenceFileRepository.findAll());
		
		 if (id != null) {
		        ReferenceFile rf = referenceFileRepository.findById(id).orElse(null);
		        model.addAttribute("detail", rf);
		    }
		
		return "admin/referenceManager";
	}
	
	
	@PostMapping("/referenceFileInsert")
	public String referenceFileInsert(
			MultipartFile file,
			ReferenceFile f,
			Model model,
			RedirectAttributes rttr
			) throws IllegalStateException, IOException {
		
		referenceFileService.referenceFileInsert(file, f);
		rttr.addFlashAttribute("msg", "파일이 등록 되었습니다.");

		return "redirect:/admin/referenceManager";
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
	
	
	@PostMapping("/referenceUpdate")
	public String updateReferenceFile(
	        @RequestParam("id") Long id,
	        @RequestParam("filesubject") String filesubject,
	        @RequestParam(value = "file", required = false) MultipartFile file
	) throws IOException {
	    

	    ReferenceFile ref = referenceFileRepository.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("해당 파일 없음: " + id));

	    // 제목 수정
	    ref.setFilesubject(filesubject);

	    // 파일 교체 (선택)
	    if (file != null && !file.isEmpty()) {

	        // 기존 파일 삭제
	        if (ref.getFilepath() != null) {
	            File oldFile = new File(ref.getFilepath());
	            if (oldFile.exists()) oldFile.delete();
	        }

	        String originName = file.getOriginalFilename();
	        String ext = originName.substring(originName.lastIndexOf(".")).toLowerCase();
	        String savedName = UUID.randomUUID() + "_" + originName;

	        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
	        String absolutePath = new File("").getAbsolutePath() + File.separator;
	        String path = absolutePath + "src/main/resources/static/administration/reference/" + currentDate;
	        String road = "/upload/reference/" + currentDate;

	        File dir = new File(path);
	        if (!dir.exists()) dir.mkdirs();

	        File dest = new File(dir, savedName);
	        file.transferTo(dest);

	        ref.setFilename(originName);
	        ref.setFileextension(ext);
	        ref.setFileroad(road + "/" + savedName);  
	        ref.setFilepath(dest.getAbsolutePath());
	        ref.setFiledate(new Date());             
	    }

	    referenceFileRepository.save(ref);
	    return "redirect:/admin/referenceManager";
	}

}
