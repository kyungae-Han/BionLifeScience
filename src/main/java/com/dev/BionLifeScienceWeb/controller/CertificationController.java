package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import com.dev.BionLifeScienceWeb.model.CertificationSort;
import com.dev.BionLifeScienceWeb.repository.CertificationRepository;
import com.dev.BionLifeScienceWeb.repository.CertificationSortRepository;
import com.dev.BionLifeScienceWeb.service.CertificationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class CertificationController {
	
	private final CertificationRepository certificationRepository;
	private final CertificationSortRepository certificationSortRepository;
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
	public String certificationInsertForm(
			Model model
			) {
		model.addAttribute("sorts", certificationSortRepository.findAllByOrderBySortIndexAscIdAsc());
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
		model.addAttribute("sorts", certificationSortRepository.findAllByOrderBySortIndexAscIdAsc());
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

	//****************************** 인증서 분류 관리
	// 공지사항 분류 관리(noticeSubjectManager)와 같은 자리의 화면이다.
	// 다른 점은 영문 이름과 순서가 있고, 쓰는 데가 있으면 삭제를 막는다는 것이다.

	@GetMapping("/certificationSortManager")
	public String certificationSortManager(
			Model model
			) {
		List<CertificationSort> sorts = certificationSortRepository.findAllByOrderBySortIndexAscIdAsc();

		// 분류마다 쓰는 인증서가 몇 건인지 같이 보낸다.
		// 삭제가 막히는 이유를 누르기 전에 알 수 있어야 한다.
		// 분류 수가 많아야 열 몇 개라 한 번씩 세도 된다
		Map<Long, Long> useCounts = new LinkedHashMap<>();
		for (CertificationSort s : sorts) {
			useCounts.put(s.getId(), certificationRepository.countBySortId(s.getId()));
		}

		model.addAttribute("sorts", sorts);
		model.addAttribute("useCounts", useCounts);
		return "admin/certificationSortManager";
	}

	/** 분류를 새로 만든다. 목록 맨 뒤에 붙는다. 순서는 끌어서 바꾼다 */
	@PostMapping("/certificationSortInsert")
	@ResponseBody
	public String certificationSortInsert(
			@RequestParam("nameKo") String nameKo,
			@RequestParam(value="nameEn", required=false) String nameEn
			) {

		String ko = nameKo == null ? "" : nameKo.trim();
		if (ko.isEmpty()) {
			return "EMPTY";
		}
		if (certificationSortRepository.existsByNameKo(ko)) {
			return "DUP";
		}

		CertificationSort sort = new CertificationSort();
		sort.setNameKo(ko);
		sort.setNameEn(nameEn == null || nameEn.isBlank() ? null : nameEn.trim());
		sort.setSortIndex(nextSortIndex());
		certificationSortRepository.save(sort);
		return "ok";
	}

	/**
	 * 이름을 고친다. 인증서는 id 로 물려 있으므로 이름을 바꿔도 따라온다.
	 * 다만 예전 방식의 사본(CERTIFICATION_SORT)도 같이 맞춰 둔다
	 */
	@PostMapping("/certificationSortUpdate")
	@ResponseBody
	public String certificationSortUpdate(
			@RequestParam("id") Long id,
			@RequestParam("nameKo") String nameKo,
			@RequestParam(value="nameEn", required=false) String nameEn
			) {

		CertificationSort sort = certificationSortRepository.findById(id).orElse(null);
		if (sort == null) {
			return "NONE";
		}

		String ko = nameKo == null ? "" : nameKo.trim();
		if (ko.isEmpty()) {
			return "EMPTY";
		}
		if (!ko.equals(sort.getNameKo()) && certificationSortRepository.existsByNameKo(ko)) {
			return "DUP";
		}

		sort.setNameKo(ko);
		sort.setNameEn(nameEn == null || nameEn.isBlank() ? null : nameEn.trim());
		certificationSortRepository.save(sort);

		// 예전 칸을 쓰는 곳이 남아 있을 수 있어 사본을 맞춰 둔다
		for (Certification c : certificationRepository.findAll()) {
			if (id.equals(c.getSortId()) && !ko.equals(c.getSort())) {
				c.setSort(ko);
				certificationRepository.save(c);
			}
		}
		return "ok";
	}

	/**
	 * 분류를 지운다. 그 분류를 쓰는 인증서가 있으면 막는다.
	 * 지워 버리면 인증서가 분류를 잃고 어느 탭에도 안 잡히기 때문이다
	 */
	@PostMapping("/certificationSortDelete")
	@ResponseBody
	public String certificationSortDelete(@RequestParam("id") Long id) {

		long used = certificationRepository.countBySortId(id);
		if (used > 0) {
			return "USED:" + used;
		}
		certificationSortRepository.deleteById(id);
		return "ok";
	}

	/** 끌어 놓은 순서를 저장한다. 인증서 목록의 certificationOrder 와 같은 방식이다 */
	@PostMapping("/certificationSortOrder")
	@ResponseBody
	public String certificationSortOrder(@RequestParam("ids") List<Long> ids) {
		int index = 1;
		for (Long id : ids) {
			CertificationSort sort = certificationSortRepository.findById(id).orElse(null);
			if (sort != null) {
				sort.setSortIndex(index++);
				certificationSortRepository.save(sort);
			}
		}
		return "ok";
	}

	/** 목록 맨 뒤 번호. 한 건도 없으면 1 이다 */
	private int nextSortIndex() {
		return certificationSortRepository.findAll().stream()
				.map(CertificationSort::getSortIndex)
				.filter(i -> i != null)
				.mapToInt(Integer::intValue)
				.max().orElse(0) + 1;
	}
}
