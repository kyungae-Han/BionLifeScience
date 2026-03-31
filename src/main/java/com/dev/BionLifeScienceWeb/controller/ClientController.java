package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.Client;
import com.dev.BionLifeScienceWeb.model.CompanyEmail;
import com.dev.BionLifeScienceWeb.repository.CompanyEmailRepository;
import com.dev.BionLifeScienceWeb.service.ClientService;
import com.dev.BionLifeScienceWeb.service.EmailService;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
public class ClientController {

	private static final Logger log = LoggerFactory.getLogger(ClientController.class);

	private final CompanyEmailRepository companyEmailRepository;
	private final ClientService clientService;
	private final EmailService emailService;

	// 이메일 형식 검증 패턴
	private static final Pattern EMAIL_PATTERN = Pattern.compile(
		"^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$"
	);

	// 전화번호 형식 검증 패턴 (숫자, 하이픈, 공백, 괄호, +만 허용)
	private static final Pattern PHONE_PATTERN = Pattern.compile(
		"^[0-9+\\-()\\s]{7,20}$"
	);

	// 실행 가능 파일 확장자 (보안 위협)
	private static final List<String> BLOCK_EXT = List.of(
		"php", "exe", "scr", "bat", "cmd", "js", "jse", "vbs", "vbe", "jar",
		"com", "msi", "dll", "cpl", "hta", "inf", "lnk", "pif", "ps1",
		"psm1", "reg", "rgs", "sct", "shb", "shs", "wsc", "wsf", "wsh"
	);

	// 매크로 포함 가능 문서 확장자 (스미싱 위험)
	private static final List<String> MACRO_EXT = List.of(
		"docm", "xlsm", "pptm", "dotm", "xltm", "potm", "xlam", "ppam",
		"sldm", "accde", "accdr", "mdb", "mde"
	);

	// 허용 확장자 (안전한 문서 형식만)
	private static final List<String> ALLOW_EXT = List.of(
		"doc", "docx", "xls", "xlsx", "ppt", "pptx",
		"pdf", "jpg", "jpeg", "png", "gif",
		"txt", "zip", "rar", "7z"
	);

	@PostMapping("/clientInsert")
	public void clientInsert(
	        Client client,
	        MultipartFile file,
	        HttpServletRequest request,
	        HttpServletResponse response
	) throws IOException {

		String clientIp = extractClientIp(request);

		// === 입력값 검증 ===

		// 필수 항목 검증
		if (isBlank(client.getName()) || isBlank(client.getEmail())
				|| isBlank(client.getPhone()) || isBlank(client.getSubject())
				|| isBlank(client.getComment())) {
			writeAlert(response, "필수 항목을 모두 입력해 주세요.");
			return;
		}

		// 이메일 형식 검증
		if (!EMAIL_PATTERN.matcher(client.getEmail().trim()).matches()) {
			writeAlert(response, "올바른 이메일 형식을 입력해 주세요.");
			return;
		}

		// 전화번호 형식 검증
		if (!PHONE_PATTERN.matcher(client.getPhone().trim()).matches()) {
			writeAlert(response, "올바른 전화번호를 입력해 주세요.");
			return;
		}

		// HTML/스크립트 태그 포함 여부 검증 (XSS 방지)
		if (containsHtmlTags(client.getName()) || containsHtmlTags(client.getCompany())
				|| containsHtmlTags(client.getSubject())) {
			log.warn("[보안] HTML 태그 포함 요청 차단 - IP: {}", clientIp);
			writeAlert(response, "입력값에 허용되지 않는 문자가 포함되어 있습니다.");
			return;
		}

		// === 첨부파일 검증 ===
		String filename = null;
		String ext = null;

		if(file != null && !file.isEmpty()) {

			filename = file.getOriginalFilename().toLowerCase();

			if(filename.contains(".")) {
				ext = filename.substring(filename.lastIndexOf(".")+1).toLowerCase();
			}

			// 이중 확장자 검사 (예: document.pdf.exe)
			long dotCount = filename.chars().filter(ch -> ch == '.').count();
			if (dotCount > 1) {
				log.warn("[보안] 이중 확장자 파일 업로드 시도 차단 - IP: {}, 파일명: {}", clientIp, filename);
				writeAlert(response, "보안상 이중 확장자 파일은 업로드할 수 없습니다.");
				return;
			}

			if (BLOCK_EXT.contains(ext) || MACRO_EXT.contains(ext)) {
				log.warn("[보안] 차단 확장자 파일 업로드 시도 - IP: {}, 확장자: {}", clientIp, ext);
				writeAlert(response, "보안상 해당 확장자 파일은 업로드할 수 없습니다.");
				return;
			}

			if(!ALLOW_EXT.contains(ext)) {
				writeAlert(response, "지원되지 않는 파일 형식입니다. PDF, DOCX, XLSX, ZIP 등 정상 문서만 업로드 가능합니다.");
				return;
			}

		}

		// === 정상 처리 ===
		log.info("[고객문의] 접수 - IP: {}, 이름: {}, 이메일: {}", clientIp, client.getName(), client.getEmail());

	    clientService.clientInsert(client, file);
	    List<CompanyEmail> list = companyEmailRepository.findAll();

	    ExecutorService executorService = Executors.newCachedThreadPool();
	    executorService.submit(() -> {
	        String[] to = list.stream().map(CompanyEmail::getEmail).toArray(String[]::new);
	        try {
	            emailService.sendClientMail(to, client);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    });
	    executorService.shutdown();

	    response.setContentType("text/html; charset=UTF-8");
	    response.getWriter().write("""
	        <script>
	            alert('문의가 정상적으로 접수되었습니다.');
	            window.location.href = '/index';
	        </script>
	    """);
	}

	private String extractClientIp(HttpServletRequest request) {
		String[] headerNames = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
		for (String header : headerNames) {
			String ip = request.getHeader(header);
			if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
				return ip.split(",")[0].trim();
			}
		}
		return request.getRemoteAddr();
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private boolean containsHtmlTags(String value) {
		if (value == null) return false;
		return value.matches(".*<[^>]+>.*");
	}

	private void writeAlert(HttpServletResponse response, String message) throws IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().write(
			"<script>alert('" + message.replace("'", "\\'") + "'); history.back();</script>"
		);
	}
	
    @PostMapping("/clientDelete")
    public ResponseEntity<String> clientDelete(@RequestParam("id") Long id) {
        try {
            clientService.deleteClient(id);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }
}
