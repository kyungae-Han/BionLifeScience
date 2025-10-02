package com.dev.BionLifeScienceWeb.service;

import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import com.dev.BionLifeScienceWeb.model.Client;

@Service
public class EmailService {

    private final EmailSendService emailSendService;

    public EmailService(EmailSendService emailSendService) {
        this.emailSendService = emailSendService;
    }

    public void sendEmail(String[] to, String subject, String message, String replyTo)
            throws MailSendException, InterruptedException {
        emailSendService.send(to, subject, message, replyTo);
    }

    // Client 객체 그대로 받아서 메일 발송
    public void sendClientMail(String[] to, Client client)
            throws MailSendException, InterruptedException {
        String message = buildClientMailBody(client);
        emailSendService.send(
                to,
                client.getCompany() + " - " + client.getSubject(),
                message,
                client.getEmail()
        );
    }

    private String buildClientMailBody(Client client) {
    	
    	String fileInfo = "첨부 없음";

        if (client.getFilename() != null && !client.getFilename().isEmpty()) {
            fileInfo = String.format(
                "<a href='%s' download>%s</a>",
                client.getFileroad(),
                client.getFilename()
            );
        }
    	
        return """
			<div style="font-family:'Noto Sans KR',sans-serif; line-height:1.6; font-size:14px; color:#333;">
	          <h2 style="margin-bottom:10px; color:#2a4d8f;">신규 고객 문의가 접수되었습니다</h2>
	          <table style="width:100%; border-collapse:collapse; margin-bottom:15px;">
	            <tr><th style="text-align:left; width:20%; padding:6px; background:#f9f9f9;">이름</th><td style="padding:6px;">%s</td></tr>
	            <tr><th style="text-align:left; padding:6px; background:#f9f9f9;">회사</th><td style="padding:6px;">%s</td></tr>
	            <tr><th style="text-align:left; padding:6px; background:#f9f9f9;">이메일</th><td style="padding:6px;">%s</td></tr>
	            <tr><th style="text-align:left; padding:6px; background:#f9f9f9;">전화번호</th><td style="padding:6px;">%s</td></tr>
	            <tr><th style="text-align:left; padding:6px; background:#f9f9f9;">국가</th><td style="padding:6px;">%s</td></tr>
	            <tr><th style="text-align:left; padding:6px; background:#f9f9f9;">문의 주제</th><td style="padding:6px;">%s</td></tr>
	            <tr><th style="text-align:left; padding:6px; background:#f9f9f9;">문의 제목</th><td style="padding:6px;">%s</td></tr>
	            <tr><th style="text-align:left; padding:6px; background:#f9f9f9;">등록일</th><td style="padding:6px;">%s</td></tr>
	            <tr><th style="text-align:left; padding:6px; background:#f9f9f9;">첨부파일</th><td style="padding:6px;">%s</td></tr>
	          </table>
	
	          <h3 style="margin:10px 0 5px; color:#2a4d8f;">문의 내용</h3>
	          <div style="border:1px solid #ddd; padding:12px; border-radius:6px; background:#fafafa;">
	            %s
	          </div>
	        </div>
            """.formatted(
                safe(client.getName()),
                safe(client.getCompany()),
                safe(client.getEmail()),
                safe(client.getPhone()),
                safe(client.getCountry()),
                safe(client.getTopic()),
                safe(client.getSubject()),
                safe(client.getComment()),
                fileInfo,
                safe(client.getFiledate())
        );
    }

    
    private String safe(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }
}
