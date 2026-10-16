package com.dev.BionLifeScienceWeb.handler;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.dev.BionLifeScienceWeb.model.ControllerException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ControllerExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ControllerExceptionHandler.class);

	/**
	 * 예전에는 "SQL Exception" 다섯 글자만 돌려주고 끝냈다. 스프링은 이렇게 처리된
	 * 예외를 로그에 남기지 않으므로, 화면에도 로그에도 이유가 없어 원인을 찾을 수 없었다.
	 * 돌려주는 값은 그대로 두고 로그만 남긴다. 화면 동작은 바뀌지 않는다.
	 */
	@ExceptionHandler(value= {SQLException.class})
	public ResponseEntity<Object> handleSqlException(SQLException e, HttpServletRequest request){

		HttpStatus status = HttpStatus.BAD_REQUEST;

		Throwable root = e;
		while (root.getCause() != null && root.getCause() != root) {
			root = root.getCause();
		}
		log.error("SQL 오류 [{} {}] 상태코드 {} / SQL상태 {} / 원인: {}: {}",
				request.getMethod(), request.getRequestURI(),
				e.getErrorCode(), e.getSQLState(),
				root.getClass().getName(), root.getMessage(), e);

		ControllerException controllerException = new ControllerException(
				"SQL Exception",
				status,
                ZonedDateTime.now(ZoneId.of("Z"))
				);

		return new ResponseEntity<>(controllerException, status);
	}
}
