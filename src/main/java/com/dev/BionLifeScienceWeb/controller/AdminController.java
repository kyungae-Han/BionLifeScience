package com.dev.BionLifeScienceWeb.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dev.BionLifeScienceWeb.model.Client;
import com.dev.BionLifeScienceWeb.model.CompanyEmail;
import com.dev.BionLifeScienceWeb.model.CompanyInfo;
import com.dev.BionLifeScienceWeb.model.HistoryContent;
import com.dev.BionLifeScienceWeb.model.HistorySubject;
import com.dev.BionLifeScienceWeb.model.Notice;
import com.dev.BionLifeScienceWeb.model.NoticeSubject;
import com.dev.BionLifeScienceWeb.repository.ClientRepository;
import com.dev.BionLifeScienceWeb.repository.CompanyEmailRepository;
import com.dev.BionLifeScienceWeb.repository.CompanyInfoRepository;
import com.dev.BionLifeScienceWeb.repository.HistoryContentRepository;
import com.dev.BionLifeScienceWeb.repository.HistorySubjectRepository;
import com.dev.BionLifeScienceWeb.repository.NoticeRepository;
import com.dev.BionLifeScienceWeb.repository.NoticeSubjectRepository;
import com.dev.BionLifeScienceWeb.service.ClientService;
import com.dev.BionLifeScienceWeb.service.CompanyInfoService;
import com.dev.BionLifeScienceWeb.service.NoticeService;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	HistorySubjectRepository historySubjectRepository;
	
	@Autowired
	HistoryContentRepository historyContentRepository;
	
	@Autowired
	CompanyInfoRepository companyInfoRepository;
	
	@Autowired
	CompanyInfoService companyInfoService;
	
	@Autowired
	CompanyEmailRepository companyEmailRepository;
	
	@Autowired
	ClientRepository clientRepository;
	
	@Autowired
	ClientService clientService;
	
	@Autowired
	NoticeSubjectRepository noticeSubjectRepository;
	
	@Autowired
	NoticeRepository noticeRepository;
	
	@Autowired
	NoticeService noticeService;
	
	@RequestMapping({"/index" , "", "/clientManager"})
	public String adminIndex(
			Model model,
			@PageableDefault(size = 10) Pageable pageable,
			@RequestParam(required = false) String searchType,
			@RequestParam(required = false) String businessWord, 
			@RequestParam(required = false) String searchWord,
			@RequestParam(required = false) String startDate, 
			@RequestParam(required = false) String endDate
			) throws ParseException {
		
		Page<Client> clients = null;
		if(searchType==null || "none".equals(searchType)) {
			clients = clientRepository.findAllByOrderByJoindateDesc(pageable);
		}else if("name".equals(searchType)) {
			if("".equals(searchWord)) {
				clients = clientRepository.findAllByOrderByJoindateDesc(pageable);
			}else {
				clients = clientRepository.findAllByNameOrderByJoindateDesc(pageable, searchWord);
			}
		}else if("phone".equals(searchType)) {
			if("".equals(searchWord)) {
				clients = clientRepository.findAllByOrderByJoindateDesc(pageable);
			}else {
				clients = clientRepository.findAllByPhoneOrderByJoindateDesc(pageable, searchWord);
			}
		}else if("email".equals(searchType)) {
			if("".equals(searchWord)) {
				clients = clientRepository.findAllByOrderByJoindateDesc(pageable);
			}else {
				clients = clientRepository.findAllByEmailOrderByJoindateDesc(pageable, searchWord);
			}
		}else if("business".equals(searchType)) {
			clients = clientRepository.findAllByCompanyOrderByJoindateDesc(pageable, businessWord);
		}else if("period".equals(searchType)) {
			clients = clientService.findByDate(pageable, startDate, endDate);
		}else {
			clients = clientRepository.findAllByOrderByJoindateDesc(pageable);
		}
		
		int startPage = Math.max(1, clients.getPageable().getPageNumber() - 4);
		int endPage = Math.min(clients.getTotalPages(), clients.getPageable().getPageNumber() + 4);
		model.addAttribute("clients", clients);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("searchType", searchType);

		return "admin/index";
	}
	
	@RequestMapping("/clientDetail/{id}")
	public String clientDetail(
			@PathVariable Long id,
			Model model
			) {
		
		model.addAttribute("client", clientRepository.findById(id).get());
		return "admin/clientDetail";
	}
	
	@RequestMapping("/fileDownload/{id}")
	public ResponseEntity<Object> download(
			@PathVariable Long id
			) throws UnsupportedEncodingException{
		String path = "";
		String fileName = "";
		if(clientRepository.findById(id).isPresent()) {
			path = clientRepository.findById(id).get().getFilepath();
			fileName = clientRepository.findById(id).get().getFilename();
			fileName = URLEncoder.encode(fileName, "UTF-8");
		}
		
		try {
			Path filePath = Paths.get(path);
			Resource resource = new InputStreamResource(Files.newInputStream(filePath)); // 파일 resource 얻기
			
//			File file = new File(path);
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentDisposition(ContentDisposition.builder("attachment").filename(fileName).build());  // 다운로드 되거나 로컬에 저장되는 용도로 쓰이는지를 알려주는 헤더
			
			return new ResponseEntity<Object>(resource, headers, HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<Object>(null, HttpStatus.CONFLICT);
		}
	}
	
	@RequestMapping("/memberLoginForm")
	public String memberLoginForm() {
		return "admin/login";
	}
	
	@RequestMapping("/historyManager")
	public String historyManager(
			Model model
			){
		
		List<HistorySubject> subject = historySubjectRepository.findAllByOrderByStartDesc();
		for(HistorySubject s : subject) {
			s.setContents(historyContentRepository.findAllBySubjectIdOrderByDateDesc(s.getId()));
		}
		model.addAttribute("list",subject);
		return "admin/historyManager";
	}
	
	@RequestMapping("/historySubjectInsert")
	public String historySubjectInsert(
			HistorySubject historySubject,
			Model model
			) {
		
		historySubjectRepository.save(historySubject);
		List<HistorySubject> subject = historySubjectRepository.findAllByOrderByStartDesc();
		for(HistorySubject s : subject) {
			s.setContents(historyContentRepository.findAllBySubjectIdOrderByDateDesc(s.getId()));
		}
		model.addAttribute("list",subject);
		
		return "admin/historyManager :: #history-subject-wrap";
	}
	
	@RequestMapping("/historyContentInsert")
	public String historyContentInsert(
			HistoryContent historyContent,
			String dateString,
			Model model
			) throws ParseException {
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = format.parse(dateString);
		historyContent.setDate(date);
		historyContent.setHistoryDate(dateString);
		historyContent.setSubjectId(historyContent.getSubjectId());
		historyContentRepository.save(historyContent);
		
		List<HistorySubject> subject = historySubjectRepository.findAllByOrderByStartDesc();
		for(HistorySubject s : subject) {
			s.setContents(historyContentRepository.findAllBySubjectIdOrderByDateDesc(s.getId()));
		}
		model.addAttribute("list",subject);
		
		return "admin/historyManager :: #history-subject-wrap";
		
	}
	
	@RequestMapping("/historyDelete")
	public String historyDelete(
			Long id,
			int code,
			Model model
			) {
		
		if(code == 1) {
			historySubjectRepository.deleteById(id);
		}else if(code == 0){
			historyContentRepository.deleteById(id);
		}
		
		List<HistorySubject> subject = historySubjectRepository.findAllByOrderByStartDesc();
		for(HistorySubject s : subject) {
			s.setContents(historyContentRepository.findAllBySubjectIdOrderByDateDesc(s.getId()));
		}
		model.addAttribute("list",subject);
		
		return "admin/historyManager :: #history-subject-wrap";
	}
	
	@RequestMapping("/noticeSubjectInsert")
	public String noticeSubjectInsert(
			NoticeSubject noticeSubject,
			Model model
			) {
		
		noticeSubjectRepository.save(noticeSubject);
		model.addAttribute("subject", noticeSubjectRepository.findAll());
		
		return "admin/noticeSubjectManager :: #textForm";
	}
	
	@RequestMapping("/noticeSubjectDelete")
	public String noticeSubjectDelete(
			@RequestParam(value="text[]") Long[] text,
			Model model
			){
		
		for(Long id : text) {
			noticeSubjectRepository.deleteById(id);
		}
		model.addAttribute("subject", noticeSubjectRepository.findAll());
		
		return "admin/noticeSubjectManager :: #textForm";
		
	}
	
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseBody
	public String SQLExceptionController() {
		

		return "fail";
	}
	
	@RequestMapping("/noticeSubjectManager")
	public String noticeSubjectManager(
			Model model
			) {
		
		model.addAttribute("subject",noticeSubjectRepository.findAll());
		return "admin/noticeSubjectManager";
	}
	
	@RequestMapping("/noticeManager")
	public String noticeManager(
			Model model,
			@PageableDefault(size = 10) Pageable pageable
			) {
		
		Page<Notice> notices = noticeRepository.findAllByOrderByDateDesc(pageable);
		
		int startPage = Math.max(1, notices.getPageable().getPageNumber() - 4);
		int endPage = Math.min(notices.getTotalPages(), notices.getPageable().getPageNumber() + 4);
		model.addAttribute("notices", notices);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		
		return "admin/noticeManager";
	}
	
	@RequestMapping("/noticeUpdate")
	public String noticeUpdate(
			Notice notice,
			Model model
			) {
		
		noticeService.noticeUpdate(notice);
		model.addAttribute("subject", noticeSubjectRepository.findAll());
		model.addAttribute("notice", noticeRepository.findById(notice.getId()).get());
		return "admin/noticeDetail";
	}
	
	@RequestMapping("/noticeDetail/{id}")
	public String noticeDetail(
			@PathVariable Long id,
			Model model
			) {
		model.addAttribute("subject", noticeSubjectRepository.findAll());
		model.addAttribute("notice", noticeRepository.findById(id).get());
		return "admin/noticeDetail";
	}
	
	@RequestMapping("/noticeInsertForm")
	public String noticeInsertForm(
			Model model
			) {
		
		model.addAttribute("subject", noticeSubjectRepository.findAll());
		return "admin/noticeInsertForm";
	}
	
	@RequestMapping("/noticeInsert")
	@ResponseBody
	public String noticeInsert(
			Notice notice
			) {
		
		notice.setDate(new Date());
		notice.setNoticeSubject(noticeSubjectRepository.findById(notice.getSubjectId()).get());
		noticeRepository.save(notice);
		StringBuffer sb = new StringBuffer();
		String msg = "공지사항이 등록 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/noticeManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
		
	}
	
	@RequestMapping("/noticeDelete/{id}")
	@ResponseBody
	public String noticeDelete(
			@PathVariable Long id
			) {
		
		noticeRepository.deleteById(id);
		StringBuffer sb = new StringBuffer();
		String msg = "공지사항이 삭제 되었습니다.";

		sb.append("alert('" + msg + "');");
		sb.append("location.href='/admin/noticeManager'");
		sb.append("</script>");
		sb.insert(0, "<script>");

		return sb.toString();
		
	}
	
	@RequestMapping("/siteManager")
	public String siteManager(
			Model model
			) {
		if(companyInfoRepository.findById(1L).isPresent()) {
			model.addAttribute("company",companyInfoRepository.findById(1L).get());
		}else {
			model.addAttribute("company",new CompanyInfo());
		}
		model.addAttribute("email",companyEmailRepository.findAll());
		return "admin/siteManager";
	}
	
	@RequestMapping("/companyInfoInsert")
	public String companyInfoInsert(
			CompanyInfo companyInfo,
			Model model
			) {
		companyInfoService.changeCompanyInfo(companyInfo);

		if(companyInfoRepository.findById(1L).isPresent()) {
			model.addAttribute("company",companyInfoRepository.findById(1L).get());
		}else {
			model.addAttribute("company",new CompanyInfo());
		}
		return "admin/siteManager :: #companyInfoForm";
	}
	
	@RequestMapping("/emailInsert")
	public String emailInsert(
			CompanyEmail companyEmail,
			Model model
			) {
		companyEmailRepository.save(companyEmail);
		model.addAttribute("email", companyEmailRepository.findAll());
		model.addAttribute("company",companyInfoRepository.findById(1L).get());
		return "admin/siteManager :: #emailForm";
	}
	
	@RequestMapping("/deleteEmail")
	public String deleteEmail(
			@RequestParam(value="email[]") Long[] email,
			Model model
			) {
		
		for(Long id : email) {
			companyEmailRepository.deleteById(id);
		}
		model.addAttribute("email", companyEmailRepository.findAll());
		model.addAttribute("company",companyInfoRepository.findById(1L).get());
		return "admin/siteManager :: #emailForm";
	}
	
	@RequestMapping("/changeEmailStatus")
	public String changeEmailStatus(
			Model model,
			Boolean companyEmailCheck
			) {
		
		companyInfoService.changeCompanyMail(companyEmailCheck);
		if(companyInfoRepository.findById(1L).isPresent()) {
			model.addAttribute("company",companyInfoRepository.findById(1L).get());
		}else {
			model.addAttribute("company",new CompanyInfo());
		}
		
		return "admin/siteManager :: #companyEmailCheck";
	}
	

	
}
