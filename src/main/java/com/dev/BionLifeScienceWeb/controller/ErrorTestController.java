package com.dev.BionLifeScienceWeb.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dev.BionLifeScienceWeb.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/sample")
@Controller
@Slf4j
public class ErrorTestController {

	private final EmailService emailService;
	
	public ErrorTestController(EmailService emailService) {
        this.emailService = emailService;
    }
	
	@RequestMapping("/404")
	public String nonePage() {
		
		return "error/404";
	}
	
	@RequestMapping("/403")
	public String noneAuth() {
		
		return "error/403";
	}
	
	@RequestMapping("/500")
	public String serverError() {
		
		return "error/500";
	}
	
	@RequestMapping("/mail")
	@ResponseBody
	public String main() throws InterruptedException {
		
		ExecutorService executorService = Executors.newCachedThreadPool();

        // 작업1 (스레드)
        executorService.submit(() -> {
        	String[] to = new String[1];
    		to[0] = "admin@atrt.co.kr";
    		
    		try {
    			emailService.sendEmail(to, "ㅎㅇㅎㅇ", "ㅎㅇㅎㅇ");
    		}catch(MailSendException e) {
    			System.out.println(e);
    		} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        });

        executorService.shutdown();
		
        return "success";
	}
}
