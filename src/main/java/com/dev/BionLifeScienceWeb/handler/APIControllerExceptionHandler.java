package com.dev.BionLifeScienceWeb.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class APIControllerExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(APIControllerExceptionHandler.class);

	/**
	 * 예전에는 "fail" 네 글자만 돌려주고 끝냈다. 스프링은 이렇게 처리된 예외를
	 * 로그에 남기지 않으므로, 화면에도 로그에도 이유가 없어 원인을 찾을 수 없었다.
	 * 돌려주는 값은 그대로 두고 로그만 남긴다. 화면 동작은 바뀌지 않는다.
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public String handleDataIntegrityViolationException(DataIntegrityViolationException ex,
			HttpServletRequest request) {
		Throwable root = ex;
		while (root.getCause() != null && root.getCause() != root) {
			root = root.getCause();
		}
		log.error("DB 제약 위반 [{} {}] 원인: {}: {}",
				request.getMethod(), request.getRequestURI(),
				root.getClass().getName(), root.getMessage(), ex);
		return "fail";
	}
}
