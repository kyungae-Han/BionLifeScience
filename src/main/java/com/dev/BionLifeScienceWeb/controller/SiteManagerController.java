package com.dev.BionLifeScienceWeb.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
		
		
		List<Banner> banners = bannerRepository.findAllByOrderByBannerIndexAscIdDesc();

	    for (Banner b : banners) {
	        if (b.getWebroad() != null) {
	            b.setWebroad(b.getWebroad().replace("/administration/", "/upload/"));
	        }
	        if (b.getMobileroad() != null) {
	            b.setMobileroad(b.getMobileroad().replace("/administration/", "/upload/"));
	        }
	    }
		
		// 위에서 정렬해 둔 목록을 그대로 넘긴다.
		// findAll() 을 넘기고 있어서 관리 화면만 순서가 뒤죽박죽이었다
		model.addAttribute("banners", banners);
		model.addAttribute("timestamp", System.currentTimeMillis());
		return "admin/bannerManager";
	}
	
	/** 배너 등록 화면. 목록과 분리해 두었다. 브랜드·제품 관리와 같은 방식이다 */
	@GetMapping("/bannerInsertForm")
	public String bannerInsertForm() {
		return "admin/bannerInsertForm";
	}

	@PostMapping("/bannerInsert")
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
	
	
	@GetMapping("/getBanner/{id}")
	@ResponseBody
	public Banner getBanner(@PathVariable Long id) {
	    return bannerRepository.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));
	}
	
	
	@PostMapping("/bannerUpdate/{id}")
	@ResponseBody
	public String bannerUpdate(
	        @RequestParam("id") Long id,
	        @RequestParam(value = "webFile", required = false) MultipartFile webFile,
	        @RequestParam(value = "mobileFile", required = false) MultipartFile mobileFile,
	        Banner banner
	) throws IOException {
	    bannerService.bannerUpdate(id, webFile, mobileFile, banner);
	    return "success";
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
	
	/** 줄에서 바로 고치는 노출 기간 */
	@PostMapping("/bannerPeriod/{id}")
	public String bannerPeriod(@PathVariable Long id,
			@RequestParam(value = "startDate", required = false) String startDate,
			@RequestParam(value = "endDate", required = false) String endDate,
			RedirectAttributes redirectAttributes) {

		bannerRepository.findById(id).ifPresent(b -> {
			b.setStartDate(parseDate(startDate));
			b.setEndDate(parseDate(endDate));
			bannerRepository.save(b);
		});
		redirectAttributes.addFlashAttribute("message", "노출 기간을 저장했습니다.");
		return "redirect:/admin/bannerManager";
	}

	/** 줄에서 바로 켜고 끄기 */
	@PostMapping("/bannerUse/{id}")
	public String bannerUse(@PathVariable Long id,
			@RequestParam("on") boolean on,
			RedirectAttributes redirectAttributes) {

		bannerRepository.findById(id).ifPresent(b -> {
			b.setUseYn(on);
			bannerRepository.save(b);
		});
		redirectAttributes.addFlashAttribute("message", on ? "배너를 켰습니다." : "배너를 껐습니다.");
		return "redirect:/admin/bannerManager";
	}

	/**
	 * 끌어서 바꾼 순서를 저장한다.
	 * 화면이 보낸 차례대로 1 부터 다시 매긴다. 중간에 빠진 번호가 생기지 않는다.
	 */
	@PostMapping("/bannerOrder")
	@ResponseBody
	public String bannerOrder(@RequestParam("ids") List<Long> ids) {
		int index = 1;
		for (Long id : ids) {
			Banner banner = bannerRepository.findById(id).orElse(null);
			if (banner != null) {
				banner.setBannerIndex(index++);
				bannerRepository.save(banner);
			}
		}
		return "ok";
	}

	/** 빈 칸은 null 로 둔다. 화면에서 지우면 기간 제한이 없어진다 */
	private static java.time.LocalDate parseDate(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return java.time.LocalDate.parse(value.trim());
		} catch (RuntimeException e) {
			return null;
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

