package com.dev.BionLifeScienceWeb.service;

import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final EmailSendService  emailSendService ;

    public EmailService(EmailSendService emailSendService) {
        this.emailSendService = emailSendService;
    }

    public void sendEmail(String[] to, String subject, String message) throws MailSendException, InterruptedException {
    	emailSendService.send(to, subject, message);
    }
}
