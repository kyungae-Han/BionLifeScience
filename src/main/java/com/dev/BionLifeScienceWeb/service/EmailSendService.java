package com.dev.BionLifeScienceWeb.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.dev.BionLifeScienceWeb.config.EmailSendable;

@Service
public class EmailSendService implements EmailSendable{

	private final JavaMailSender javaMailSender;

    public EmailSendService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender; // MailSender 인터페이스 상속
    }

    @Override
    public void send(String[] to, String subject, String message) {
        // 보낸 사람, 받는 사람, 참조, 제목 및 텍스트를 포함하는 메시지 생성
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        
        mailMessage.setTo(to); //받는 사람 주소
        mailMessage.setSubject(subject); //제목
        mailMessage.setText(message); //메시지 내용
        javaMailSender.send(mailMessage); //메일 발송

    }
}
