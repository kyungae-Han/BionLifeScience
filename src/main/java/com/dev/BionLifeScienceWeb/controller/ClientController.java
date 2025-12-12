package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
public class ClientController {

	private final CompanyEmailRepository companyEmailRepository;
	private final ClientService clientService;
	private final EmailService emailService;
	
	

	@PostMapping("/clientInsert")
	public void clientInsert(
	        Client client,
	        MultipartFile file,
	        HttpServletResponse response
	) throws IOException {
		
		
		final List<String> BLOCK_EXT = List.of("php", "exe", "scr", "bat", "cmd", "js", "jse", "vbs", "vbe", "jar");
		
		final List<String> MACRO_EXT = List.of("docm", "xlsm", "pptm");
		
		final List<String> ALLOW_EXT =  List.of("doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf", "jpg", "jpeg", "png", "gif", "txt", "zip", "rar", "7z");
		
		String filename = null;
		String ext = null;
		
		if(file != null && !file.isEmpty()) {
			
			filename = file.getOriginalFilename().toLowerCase();
			
			if(filename.contains(".")) {
				ext = filename.substring(filename.lastIndexOf(".")+1).toLowerCase();
			}
			
		
			if (BLOCK_EXT.contains(ext) || MACRO_EXT.contains(ext)) {
				
				response.setContentType("test/html; charset=UTF-8");
				response.getWriter().write("""
		                <script>
	                    alert('보안상 해당 확장자 파일은 업로드할 수 없습니다.');
	                    history.back();
	                </script>
	            """);
				return;
			}
			
			
			if(!ALLOW_EXT.contains(ext)) {
				response.setContentType("text/html; charset=UTF-8");
				response.getWriter().write("""
		                <script>
	                    alert('지원되지 않는 파일 형식입니다. PDF, DOCX, XLSX, ZIP 등 정상 문서만 업로드 가능합니다.');
	                    history.back();
	                </script>
	            """);
				return;
			}
			
		}
		
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
