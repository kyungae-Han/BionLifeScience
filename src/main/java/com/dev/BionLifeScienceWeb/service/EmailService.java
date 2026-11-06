package com.dev.BionLifeScienceWeb.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import com.dev.BionLifeScienceWeb.model.Client;

@Service
public class EmailService {

    private final EmailSendService emailSendService;
    
    @Value("${spring.upload.env}")
    private String env;

    @Value("${spring.upload.path}")
    private String commonPath;

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

    /**
     * 저장에 실패한 문의를 알린다.
     *
     * 이 메일이 나갔다는 것은 그 문의가 DB 에 들어가지 않았다는 뜻이다.
     * 관리자 [문의사항 관리] 에는 나오지 않으므로 이 메일이 내용의 전부다.
     * 2026-11-05 에 이모지가 든 문의가 사라졌을 때는 이 알림이 없어서 아무도 몰랐다.
     */
    public void sendClientFailureMail(String[] to, Client client, String reason)
            throws MailSendException, InterruptedException {
        String message = buildFailureNotice(reason) + buildClientMailBody(client);
        emailSendService.send(
                to,
                "[접수실패] " + safe(client.getCompany()) + " - " + safe(client.getSubject()),
                message,
                client.getEmail()
        );
    }

    private String buildFailureNotice(String reason) {
        return """
            <div style="font-family:'Noto Sans KR',sans-serif; font-size:14px; line-height:1.6;
                        border:2px solid #d64545; border-radius:6px; padding:14px; margin-bottom:16px;
                        background:#fdf3f3; color:#8a2222;">
              <strong style="font-size:15px;">이 문의는 저장되지 않았습니다.</strong><br>
              관리자 [문의사항 관리] 화면에는 나오지 않습니다.
              아래 내용이 남은 전부이므로 이 메일을 지우지 말고, 고객에게 직접 연락해 주세요.<br>
              <span style="color:#666666; font-size:12px;">원인: %s</span>
            </div>
            """.formatted(safe(reason));
    }

    private String buildClientMailBody(Client client) {
    	
    	
    	String baseUrl;
        if ("local".equals(env)) {
            baseUrl = "http://localhost:8080";
        } else {
            baseUrl = "https://bionlifescience.com";
        }

        String fileInfo = "첨부 없음";
        
        


		if (client.getFilename() != null && !client.getFilename().isEmpty()) {
			
			String[] parts = client.getFileroad().split("/");
			String savedFileName = parts[parts.length - 1];
		
		    String downloadUrl = String.format(
		        "%s/client/download?date=%s&filename=%s",
		        baseUrl,
		        URLEncoder.encode(client.getFiledate(), StandardCharsets.UTF_8),
		        URLEncoder.encode(savedFileName, StandardCharsets.UTF_8)
		    );
		
		    fileInfo = String.format(
		        "<a href='%s' target='_blank' style='color:#2a4d8f; text-decoration:none;'>%s 다운로드</a>",
		        downloadUrl,
		        client.getFilename()
		    );
		}
    	
        return """
			<div style="font-family:'Noto Sans KR',sans-serif; line-height:1.6; font-size:14px; color:#333;">
	          <h2 style="margin-bottom:10px; color:#2a4d8f;">신규 고객 문의가 접수되었습니다</h2>
	          <table style="width:100%%; border-collapse:collapse; margin-bottom:15px;">
	            <tr><th style="text-align:left; width:20%%; padding:6px; background:#f9f9f9;">이름</th><td style="padding:6px;">%s</td></tr>
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
                safe(client.getFiledate()),
                fileInfo,
                safe(client.getComment())
        );
    }

    
    private String safe(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }
}
