package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dev.BionLifeScienceWeb.controller.api.SummernoteImageProcessor;
import com.dev.BionLifeScienceWeb.model.AllowedEmailAddress;
import com.dev.BionLifeScienceWeb.model.AllowedEmailTld;
import com.dev.BionLifeScienceWeb.model.Client;
import com.dev.BionLifeScienceWeb.model.CompanyEmail;
import com.dev.BionLifeScienceWeb.model.CompanyInfo;
import com.dev.BionLifeScienceWeb.model.HistoryContent;
import com.dev.BionLifeScienceWeb.model.HistorySubject;
import com.dev.BionLifeScienceWeb.model.Member;
import com.dev.BionLifeScienceWeb.model.MemberAccount;
import com.dev.BionLifeScienceWeb.model.Notice;
import com.dev.BionLifeScienceWeb.model.NoticeSubject;
import com.dev.BionLifeScienceWeb.model.page.PageContent;
import com.dev.BionLifeScienceWeb.model.page.PageGroup;
import com.dev.BionLifeScienceWeb.repository.AllowedEmailAddressRepository;
import com.dev.BionLifeScienceWeb.repository.AllowedEmailTldRepository;
import com.dev.BionLifeScienceWeb.repository.ClientRepository;
import com.dev.BionLifeScienceWeb.repository.CompanyEmailRepository;
import com.dev.BionLifeScienceWeb.repository.CompanyInfoRepository;
import com.dev.BionLifeScienceWeb.repository.HistoryContentRepository;
import com.dev.BionLifeScienceWeb.repository.HistorySubjectRepository;
import com.dev.BionLifeScienceWeb.repository.MemberRepository;
import com.dev.BionLifeScienceWeb.repository.NoticeRepository;
import com.dev.BionLifeScienceWeb.repository.NoticeSubjectRepository;
import com.dev.BionLifeScienceWeb.service.ClientService;
import com.dev.BionLifeScienceWeb.service.CompanyInfoService;
import com.dev.BionLifeScienceWeb.service.MemberService;
import com.dev.BionLifeScienceWeb.service.NoticeService;
import com.dev.BionLifeScienceWeb.service.page.PageContentAdminService;
import com.dev.BionLifeScienceWeb.service.page.PageGroupAdminService;
import com.dev.BionLifeScienceWeb.utils.PasswordEncoding;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
	private final SummernoteImageProcessor imageProcessor;
	private final AllowedEmailTldRepository allowedEmailTldRepository;
	private final AllowedEmailAddressRepository allowedEmailAddressRepository;
	private final HistorySubjectRepository historySubjectRepository;
	private final HistoryContentRepository historyContentRepository;
	private final CompanyInfoRepository companyInfoRepository;
	private final CompanyEmailRepository companyEmailRepository;
	private final ClientRepository clientRepository;
	private final NoticeSubjectRepository noticeSubjectRepository;
	private final NoticeRepository noticeRepository;
	private final ClientService clientService;
	private final CompanyInfoService companyInfoService;
	private final NoticeService noticeService;
	private final MemberService memberService;
	private final PageContentAdminService pageContentAdminService;
	private final PageGroupAdminService pageGroupAdminService;
	
	
	private final MemberRepository memberRepository;
	private final PasswordEncoding passwordEncoder;
	
	//******************************어드민메인
	
	@RequestMapping(
		    value = {"/index", "", "/clientManager"},
		    method = {RequestMethod.GET, RequestMethod.POST}
		)
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
	
	
	//******************************사용자관리
	@GetMapping("/clientDetail/{id}")
	public String clientDetail(
			@PathVariable Long id,
			Model model
			) {
		
		model.addAttribute("client", clientRepository.findById(id).get());
		return "admin/clientDetail";
	}
	
	@RequestMapping(value = "/fileDownload/{id}",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
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
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentDisposition(ContentDisposition.builder("attachment").filename(fileName).build());  // 다운로드 되거나 로컬에 저장되는 용도로 쓰이는지를 알려주는 헤더
			
			return new ResponseEntity<Object>(resource, headers, HttpStatus.OK);
		} catch(Exception e) {
			return new ResponseEntity<Object>(null, HttpStatus.CONFLICT);
		}
	}
	
	
	//******************************어드민 로그인
	@GetMapping("/memberLoginForm")
	public String memberLoginForm() {
		
		return "admin/login";
	}
	
	@GetMapping("/memberEdit/{id}")
	public String memberEdit(@PathVariable Long id, Model model) {
	    Member member = memberRepository.findById(id).orElseThrow(() ->
	        new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
	    model.addAttribute("member", member);
	    return "admin/memberInsert"; // 같은 폼 재사용
	}
	
	@PostMapping("/memberUpdate")
	public String memberUpdate(@ModelAttribute Member member) {
	    memberService.updateMember(member);
	    return "redirect:/admin/memberList";
	}

	
	@GetMapping("/passwordForm")
	public String passwordForm() {
	    return "admin/passwordForm";
	}

	@PostMapping("/passwordUpdate")
	public String updatePassword(@RequestParam String oldPwd,
	                             @RequestParam String newPwd) {

	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    Object principal = authentication.getPrincipal();

	    Member member = null;

	    if (principal instanceof MemberAccount) {
	        member = ((MemberAccount) principal).getMember();
	    } else if (principal instanceof Member) {
	        // 혹시 Member 자체로 로드되었을 경우 fallback
	        member = (Member) principal;
	    } else {
	        return "redirect:/memberLoginForm?error=principal";
	    }

	    Member dbMember = memberRepository.findById(member.getId()).orElse(null);
	    if (dbMember == null) {
	        return "redirect:/admin/passwordForm?error=db";
	    }

	    if (!passwordEncoder.matches(oldPwd, dbMember.getPassword())) {
	        return "redirect:/admin/passwordForm?error=wrong";
	    }

	    dbMember.setPassword(passwordEncoder.encode(newPwd));
	    memberRepository.save(dbMember);

	    SecurityContextHolder.clearContext();
	    return "redirect:/memberLoginForm?passwordChanged";
	}
	
	
	//******************************연혁관리
	@GetMapping("/historyManager")
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
	
	@GetMapping("/historySubjectInsert")
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
	
	@PostMapping("/historyContentInsert")
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
	
	@RequestMapping(value = "/historyDelete",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
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
	
	
	
	
	//******************************공지사항관리
	
	@PostMapping("/noticeSubjectInsert")
	public String noticeSubjectInsert(
			NoticeSubject noticeSubject,
			Model model
			) {
		
		noticeSubjectRepository.save(noticeSubject);
		model.addAttribute("subject", noticeSubjectRepository.findAll());
		
		return "admin/noticeSubjectManager :: #textForm";
	}
	
	@RequestMapping(value = "/noticeSubjectDelete",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
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

	@GetMapping("/noticeSubjectManager")
	public String noticeSubjectManager(
			Model model
			) {
		
		model.addAttribute("subject",noticeSubjectRepository.findAll());
		return "admin/noticeSubjectManager";
	}
	
	@GetMapping("/noticeManager")
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
	
	@PostMapping("/noticeUpdate")
	public String noticeUpdate(
			Notice notice,
			Model model
			)throws IOException {
	    String html = notice.getContent();
	    
	    
	    String processed = imageProcessor.processEditorImages(notice.getContent(), "notice");
	    notice.setContent(processed);

	    
	    // 🔹 첫 번째 이미지 경로 추출 & 가공
	    String firstImg = extractFirstImageUrl(processed);
	    if (firstImg != null) {
	        firstImg = firstImg.replaceAll("https?://[^/]+", ""); // 도메인 제거
	        firstImg = firstImg.replaceAll("(\\?[^\"'>]*)", "");  // ? 파라미터 제거
	    }
	    notice.setImageUrl(firstImg);

	    noticeService.noticeUpdate(notice);

	    model.addAttribute("subject", noticeSubjectRepository.findAll());
	    model.addAttribute("notice", noticeRepository.findById(notice.getId()).get());
	    return "admin/noticeDetail";
	}
	
	@RequestMapping(value = "/noticeDetail/{id}",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	public String noticeDetail(
			@PathVariable Long id,
			Model model
			) {
		model.addAttribute("subject", noticeSubjectRepository.findAll());
		model.addAttribute("notice", noticeRepository.findById(id).get());
		return "admin/noticeDetail";
	}
	
	@GetMapping("/noticeInsertForm")
	public String noticeInsertForm(
			Model model
			) {
		
		model.addAttribute("subject", noticeSubjectRepository.findAll());
		return "admin/noticeInsertForm";
	}
	
	
	
	@PostMapping("/noticeInsert")
	public String noticeInsert(
			Notice notice, Model model,
			RedirectAttributes rttr
			)throws IOException  {
		
		
		 String processedContent = imageProcessor.processEditorImages(notice.getContent(), "notice");
		    notice.setContent(processedContent);
		   // 본문에서 첫 번째 이미지 추출
		   String firstImg = extractFirstImageUrl(processedContent);
		    if (firstImg != null && firstImg.length() > 500) {
		        firstImg = firstImg.substring(0, 500);
		    }
		    
	    notice.setImageUrl(firstImg);

	    // 날짜 및 분류 세팅
	    notice.setDate(new Date());
	    notice.setNoticeSubject(
	        noticeSubjectRepository.findById(notice.getSubjectId())
	            .orElseThrow(() -> new IllegalArgumentException("잘못된 분류 ID"))
	    );

	    // 저장
	    noticeRepository.save(notice);

	    // 메시지 전달 후 리다이렉트
	    rttr.addFlashAttribute("msg", "공지사항이 등록되었습니다.");
	    return "redirect:/admin/noticeManager";
		
	}
	
	
	private String extractFirstImageUrl(String html) {
	    var doc = Jsoup.parse(html == null ? "" : html);
	    var img = doc.selectFirst("img[src]");
	    return (img != null) ? img.attr("src") : null;
	}
	
	
	
	@RequestMapping(value = "/noticeDelete/{id}",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
	@ResponseBody
	public String noticeDelete(
			@PathVariable Long id
			) {
		
		
		Notice notice = noticeRepository.findById(id).orElse(null);
		if(notice != null) {
			deleteNoticeImages(notice);
			noticeRepository.deleteById(id);
		}
		
		String msg = "공지사항이 삭제 되었습니다.";
		return "<script>alert('"+ msg +"'); location.href='/admin/noticeManager'</script>";
		
	}
	
	
	
	private void deleteNoticeImages(Notice notice) {
		Set<String> targets = new HashSet<>();
		
		if(notice.getImageUrl() != null) {
			targets.add(notice.getImageUrl());
		}
		
		Document doc = Jsoup.parse(notice.getContent() == null ? "" : notice.getContent());
		
		for(Element img : doc.select("img[src]")) {
			targets.add(img.attr("src"));
		}
		
		Path base = Paths.get(System.getProperty("user.dir"), "upload", "front", "images")
				.toAbsolutePath().normalize();
		
		for(String src : targets) {
			String rel = toUploadRelativePath(src);
			if(rel == null) continue;
			
			if(!rel.startsWith("notice/editor")) continue;
			
			Path filePath = base.resolve(rel).normalize();
			if(!filePath.startsWith(base)) continue;
			
			try {
				Files.deleteIfExists(filePath);
				deleteEmptyParents(filePath.getParent(), base.resolve("notice"));
			}catch(Exception ignore) {
				
			}
		}
	}
	
	private String toUploadRelativePath(String src) {
		if(src == null) return null;
		
		src = src.replaceAll("https?://[^/]+", "");
		int q = src.indexOf('?');
		
		if(q >= 0) src = src.substring(0, q);
		
		final String prefix = "/upload/";
		if(!src.startsWith(prefix)) return null;
		
		return src.substring(prefix.length());
	}
	
	private void deleteEmptyParents(Path dir, Path stopDir) throws IOException {
		while(dir != null && dir.startsWith(stopDir)) {
			try(var stream = Files.list(dir)) {
				if(stream.findAny().isPresent()) break;
			}
			
			Files.deleteIfExists(dir);
			dir = dir.getParent();
		}
	}
	
	
	//******************************사이트관리
	@GetMapping("/siteManager")
	public String siteManager(
			Model model
			) {
		if(companyInfoRepository.findById(1L).isPresent()) {
			model.addAttribute("company",companyInfoRepository.findById(1L).get());
		}else {
			model.addAttribute("company",new CompanyInfo());
		}
		model.addAttribute("email",companyEmailRepository.findAll());
		model.addAttribute("tldList", allowedEmailTldRepository.findAll());
		model.addAttribute("allowedEmailList", allowedEmailAddressRepository.findAll());
		return "admin/siteManager";
	}
	
	@RequestMapping(value = "/companyInfoInsert",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
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
	
	
	//******************************회사 이메일관리
	@PostMapping("/emailInsert")
	public String emailInsert(@RequestParam("email") String email, Model model) {
	    CompanyEmail companyEmail = new CompanyEmail();
	    companyEmail.setEmail(email);
	    companyEmailRepository.save(companyEmail);

	    model.addAttribute("email", companyEmailRepository.findAll());
	    model.addAttribute("company", companyInfoRepository.findById(1L).get());
	    return "admin/siteManager :: #emailForm";
	}
	
	@RequestMapping(value = "/deleteEmail",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
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

	//******************************허용 TLD 관리
	@PostMapping("/tldInsert")
	@ResponseBody
	public ResponseEntity<String> tldInsert(@RequestParam("tld") String tld) {
		String cleaned = tld.trim().toLowerCase().replaceAll("^\\.*", "");
		if (cleaned.isEmpty()) {
			return ResponseEntity.badRequest().body("TLD를 입력해 주세요.");
		}
		AllowedEmailTld entity = new AllowedEmailTld();
		entity.setTld(cleaned);
		allowedEmailTldRepository.save(entity);
		return ResponseEntity.ok("추가되었습니다.");
	}

	@PostMapping("/deleteTld")
	@ResponseBody
	public ResponseEntity<String> deleteTld(@RequestParam("ids[]") Long[] ids) {
		for (Long id : ids) {
			allowedEmailTldRepository.deleteById(id);
		}
		return ResponseEntity.ok("삭제되었습니다.");
	}

	//******************************허용 이메일 주소 관리
	@PostMapping("/allowedEmailInsert")
	@ResponseBody
	public ResponseEntity<String> allowedEmailInsert(@RequestParam("email") String email) {
		String cleaned = email.trim().toLowerCase();
		if (cleaned.isEmpty()) {
			return ResponseEntity.badRequest().body("이메일을 입력해 주세요.");
		}
		AllowedEmailAddress entity = new AllowedEmailAddress();
		entity.setEmail(cleaned);
		allowedEmailAddressRepository.save(entity);
		return ResponseEntity.ok("추가되었습니다.");
	}

	@PostMapping("/deleteAllowedEmail")
	@ResponseBody
	public ResponseEntity<String> deleteAllowedEmail(@RequestParam("ids[]") Long[] ids) {
		for (Long id : ids) {
			allowedEmailAddressRepository.deleteById(id);
		}
		return ResponseEntity.ok("삭제되었습니다.");
	}

	@RequestMapping(value = "/changeEmailStatus",
		    method = {RequestMethod.GET, RequestMethod.POST}
	)
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
	
	

	
	//****************************************이벤트 Page등록
	 	@GetMapping("/pageManager")
	    public String list(@RequestParam(required = false) Long groupId, Model model) {
	        model.addAttribute("groupId", groupId);
	        model.addAttribute("groupList", pageContentAdminService.getGroupList());
	        model.addAttribute("pageList", pageContentAdminService.getList(groupId));
	        return "admin/page/list";
	    }

	  	@GetMapping("/pageManager/form")
	    public String form(@RequestParam(required = false) Long id, Model model) {
	        System.out.println("pageManager form 진입");

	        PageContent page;
	        if (id != null) {
	            page = pageContentAdminService.getDetail(id);
	        } else {
	            page = new PageContent();
	            page.setPageGroup(new PageGroup());
	            page.setUseYn("Y");
	            page.setPageType("PAGE");
	            page.setPageIndex(0);
	        }

	        model.addAttribute("page", page);
	        model.addAttribute("groupList", pageContentAdminService.getGroupList());
	        return "admin/page/write";
	    }
	  	
	  	
	  	@GetMapping("/pageManager/form/{id}")
	  	public String editForm(@PathVariable Long id, Model model) {
	  	    PageContent page = pageContentAdminService.getDetail(id);

	  	    model.addAttribute("page", page);
	  	    model.addAttribute("groupList", pageContentAdminService.getGroupList());
	  	    return "admin/page/write";
	  	}

	  	@PostMapping("/pageManager")
	  	public String save(@ModelAttribute("page") PageContent page,
	  	                   @RequestParam(required = false) MultipartFile visualFile,
	  	                   RedirectAttributes redirectAttributes) {
	  	    try {
	  	        Long savedId = pageContentAdminService.save(page, visualFile);
	  	        redirectAttributes.addFlashAttribute("message", "저장되었습니다.");
	  	        return "redirect:/admin/pageManager/form/" + savedId;
	  	    } catch (Exception e) {
	  	        e.printStackTrace();
	  	        redirectAttributes.addFlashAttribute("message", e.getMessage());

	  	        if (page.getPageContentId() != null) {
	  	            return "redirect:/admin/pageManager/form/" + page.getPageContentId();
	  	        }
	  	        return "redirect:/admin/pageManager/form";
	  	    }
	  	}
	    
	    @PostMapping("/pageGroup/save")
	    @ResponseBody
	    public Map<String, Object> save(@ModelAttribute PageGroup pageGroup) {
	        Long pageGroupId = pageGroupAdminService.save(pageGroup);
	        return Map.of(
	                "result", true,
	                "pageGroupId", pageGroupId
	        );
	    }

	    @PostMapping("/pageManager/delete")
	    public String delete(@RequestParam Long id, RedirectAttributes redirectAttributes) {
	        pageContentAdminService.delete(id);
	        redirectAttributes.addFlashAttribute("message", "삭제되었습니다.");
	        return "redirect:/admin/page/list";
	    }

}


