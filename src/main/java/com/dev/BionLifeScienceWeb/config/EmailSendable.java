package com.dev.BionLifeScienceWeb.config;


/**
 *  이메일 전송을 위한 인터페이스
 *  EmailService 를 통해 구현합니다.
 */
public interface EmailSendable {
	void send(String[] to, String subject, String message) throws InterruptedException;
}
