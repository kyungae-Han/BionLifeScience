package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.Client;
import com.dev.BionLifeScienceWeb.model.CompanyEmail;
import com.dev.BionLifeScienceWeb.repository.CompanyEmailRepository;
import com.dev.BionLifeScienceWeb.service.ClientService;
import com.dev.BionLifeScienceWeb.service.EmailService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ClientController {

	private final CompanyEmailRepository companyEmailRepository;
	private final ClientService clientService;
	private final EmailService emailService;
	
	/**
	 * 스레드 처리로 이메일 발송과 DB Insert 병렬처리
	 */
	@PostMapping("/clientInsert")
	public String clientInsert(
			Client client,
			MultipartFile file
			) throws IllegalStateException, IOException {
		clientService.clientInsert(client, file);
		List<CompanyEmail> list = companyEmailRepository.findAll();
		
		ExecutorService executorService = Executors.newCachedThreadPool();

		executorService.submit(() -> {
        	String[] to = new String[list.size()];
        	int i=0;
        	for(CompanyEmail a : list) {
        		to[i] = a.getEmail();
        		i++;
        	}
        	
        	try {
        		emailService.sendEmail(to, "고객 문의 발생 - 웹서버 발송", client.toString());
        	}catch(MailSendException e) {
        		System.out.println(e);
        	} catch (InterruptedException e) {
        		e.printStackTrace();
        	}
        });

        executorService.shutdown();
		return "redirect:/contact";
	}
}
