package com.dev.BionLifeScienceWeb.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.Banner;
import com.dev.BionLifeScienceWeb.model.Event;
import com.dev.BionLifeScienceWeb.repository.BannerRepository;
import com.dev.BionLifeScienceWeb.repository.EventRepository;
import com.dev.BionLifeScienceWeb.service.BannerService;
import com.dev.BionLifeScienceWeb.service.EventService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class SiteManagerController {

	private final BannerRepository bannerRepository;
	private final BannerService bannerService;
	private final EventRepository eventRepository;
	private final EventService eventService;
	
	
	@GetMapping("/bannerManager")
	public String bannerManager(
			Model model
			) {
		model.addAttribute("banners", bannerRepository.findAll());
		return "admin/bannerManager";
	}
	
	@RequestMapping(value = "/bannerInsert", method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public String bannerInsert(
	    @RequestParam("webFile") MultipartFile webFile,
	    @RequestParam(value = "mobileFile", required = false) MultipartFile mobileFile,
	    Banner banner
	) throws IOException {

	    List<MultipartFile> files = new ArrayList<>();
	    if (webFile != null && !webFile.isEmpty()) files.add(webFile);
	    if (mobileFile != null && !mobileFile.isEmpty()) files.add(mobileFile);

	    bannerService.bannerInsert(files, banner);

	    String msg = "배너가 등록 되었습니다.";
	    String script = "<script>alert('" + msg + "');location.href='/admin/bannerManager'</script>";
	    return script;
	}
	
	@RequestMapping(value = "/deleteBanner/{id}",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public String deleteBanner(
			@PathVariable Long id,
			Model model
			) {
		
		// 1. 배너 정보 조회
	    Banner b = bannerRepository.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("배너 정보가 존재하지 않습니다."));

	    // 2. 파일 삭제 메서드 호출 (webpath, mobilepath)
	    deleteFileIfExists(b.getWebpath());

	    if (!b.getWebpath().equals(b.getMobilepath())) {
	        deleteFileIfExists(b.getMobilepath());
	    }

	    // 3. DB에서 배너 정보 삭제
	    bannerRepository.deleteById(id);

	    // 4. 스크립트 반환
	    String msg = "배너가 삭제 되었습니다.";
	    StringBuilder sb = new StringBuilder();
	    sb.append("<script>");
	    sb.append("alert('").append(msg).append("');");
	    sb.append("location.href='/admin/bannerManager';");
	    sb.append("</script>");
	    return sb.toString();
	}
	
	private void deleteFileIfExists(String path) {
	    if (path == null || path.isBlank()) return;
	    File file = new File(path);
	    if (file.exists()) {
	        boolean deleted = file.delete();
	        if (!deleted) {
	        	System.out.println("파일 삭제 실패: " + path);
	        } 
	    }
	}
	
	@GetMapping("/eventManager")
	public String eventManager(
			Model model
			) {
		if(eventRepository.findById(1L).isPresent()) {
			model.addAttribute("event", eventRepository.findById(1L).get());
		}else {
			model.addAttribute("event",new Event());
		}
		return "admin/eventManager";
	}
	
	@RequestMapping(value = "/eventInsert",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	public String eventInsert(
			Event event,
			Model model
			) {
		
		eventService.insertEvent(event);
		if(eventRepository.findById(1L).isPresent()) {
			model.addAttribute("event",eventRepository.findById(1L).get());
		}else {
			model.addAttribute("event",new Event());
		}
		return "admin/eventManager :: #eventForm";
	}
}

