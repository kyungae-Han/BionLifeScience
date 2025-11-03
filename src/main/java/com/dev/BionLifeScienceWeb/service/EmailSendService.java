package com.dev.BionLifeScienceWeb.service;

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

             helper.setTo(to);                                 // 수신자
             helper.setFrom("info@bionlifescience.com");       // SMTP 인증 계정
             helper.setReplyTo(replyTo); 
             helper.setSubject(subject);                       // 제목
             helper.setText(message, true);                    // 본문 (true = HTML 지원)

             javaMailSender.send(mimeMessage);

         } catch (MessagingException e) {
             throw new RuntimeException("메일 전송 실패", e);
         }
     }
}
