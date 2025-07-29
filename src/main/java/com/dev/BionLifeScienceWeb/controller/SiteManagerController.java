package com.dev.BionLifeScienceWeb.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.Banner;
import com.dev.BionLifeScienceWeb.model.Event;
import com.dev.BionLifeScienceWeb.repository.BannerRepository;
import com.dev.BionLifeScienceWeb.repository.EventRepository;
import com.dev.BionLifeScienceWeb.service.BannerService;
import com.dev.BionLifeScienceWeb.service.EventService;

@Controller
@RequestMapping("/admin")
public class SiteManagerController {

	@Autowired
	BannerRepository bannerRepository;
	
	@Autowired
	BannerService bannerService;
	
	@Autowired
	EventRepository eventRepository;
	
	@Autowired
	EventService eventService;
	
	
	@RequestMapping("/bannerManager")
	public String bannerManager(
			Model model
			) {
		model.addAttribute("banners", bannerRepository.findAll());
		return "admin/bannerManager";
	}
	
	@RequestMapping("/bannerInsert")
	@ResponseBody
	public String bannerInsert(
			MultipartFile webFile,
			MultipartFile mobileFile,
			Banner banner,
			Model model
			) throws IllegalStateException, IOException {
		List<MultipartFile> files = new ArrayList<MultipartFile>();
		if(mobileFile.isEmpty()) {
			files.add(webFile);
		}else {
			files.add(webFile);
			files.add(mobileFile);
		}
		bannerService.bannerInsert(files, banner);
		StringBuffer sb = new StringBuffer();
		String msg = "배너가 등록 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/bannerManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	@RequestMapping("/deleteBanner/{id}")
	@ResponseBody
	public String deleteBanner(
			@PathVariable Long id,
			Model model
			) {
		
		Banner b = bannerRepository.findById(id).get();
		if(b.getWebpath().equals(b.getMobilepath())) {
			File file = new File(b.getWebpath());
			Boolean result = file.delete();
		}else {
			File file = new File(b.getWebpath());
			File mFile = new File(b.getMobilepath());
			Boolean result = file.delete();
			Boolean mResult = mFile.delete();
		}
		
		bannerRepository.deleteById(id);
		StringBuffer sb = new StringBuffer();
		String msg = "배너가 삭제 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/bannerManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
	}
	
	@RequestMapping("/eventManager")
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
	
	@RequestMapping("/eventInsert")
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

