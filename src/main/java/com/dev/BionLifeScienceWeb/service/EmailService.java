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
			<div style="line-height:1.6;">
			  <p><strong>이름:</strong> %s</p>
			  <p><strong>회사:</strong> %s</p>
			  <p><strong>이메일:</strong> %s</p>
			  <p><strong>전화번호:</strong> %s</p>
			  <p><strong>국가:</strong> %s</p>
			  <p><strong>문의 주제:</strong> %s</p>
			  <p><strong>문의 제목:</strong> %s</p>
			  <p><strong>문의 내용:</strong> %s</p>
			  <p><strong>첨부파일:</strong> %s</p>
			  <p><strong>등록일:</strong> %s</p>
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
