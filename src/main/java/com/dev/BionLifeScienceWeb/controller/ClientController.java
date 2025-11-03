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
