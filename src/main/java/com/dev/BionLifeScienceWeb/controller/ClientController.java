package com.dev.BionLifeScienceWeb.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import com.dev.BionLifeScienceWeb.model.Client;
import com.dev.BionLifeScienceWeb.model.CompanyEmail;
import com.dev.BionLifeScienceWeb.repository.CompanyEmailRepository;
import com.dev.BionLifeScienceWeb.service.ClientService;
import com.dev.BionLifeScienceWeb.service.EmailService;

@Controller
public class ClientController {

	@Autowired
	ClientService clientService;
	
	@Autowired
	CompanyEmailRepository companyEmailRepository;
	
	@Autowired
	EmailService emailService;
	
	@PostMapping("/clientInsert")
	public String clientInsert(
			Client client,
			MultipartFile file
			) throws IllegalStateException, IOException {
		clientService.clientInsert(client, file);
		List<CompanyEmail> list = companyEmailRepository.findAll();
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		
        // 작업1 (스레드)
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
