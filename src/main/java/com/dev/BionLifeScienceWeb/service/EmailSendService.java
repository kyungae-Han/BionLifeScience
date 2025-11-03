package com.dev.BionLifeScienceWeb.service;

import java.io.UnsupportedEncodingException;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.dev.BionLifeScienceWeb.config.EmailSendable;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailSendService implements EmailSendable{

	private final JavaMailSender javaMailSender;

    public EmailSendService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender; // MailSender 인터페이스 상속
    }

    @Override
    public void send(String[] to, String subject, String message, String replyTo) {
    	 try {
             MimeMessage mimeMessage = javaMailSender.createMimeMessage();
             MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

             helper.setTo(to);                                
             helper.setFrom("admin@bionlifescience.com", "Bion Life Science");  
             helper.setReplyTo(replyTo); 
             helper.setSubject(subject);                       
             helper.setText(message, true);                   

             javaMailSender.send(mimeMessage);

         } catch (MessagingException | UnsupportedEncodingException e) {
             throw new RuntimeException("메일 전송 실패", e);
         }
     }
}
