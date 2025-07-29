package com.dev.BionLifeScienceWeb.handler;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.dev.BionLifeScienceWeb.model.ControllerException;

@ControllerAdvice
public class ControllerExceptionHandler {

	
	@ExceptionHandler(value= {SQLException.class})
	public ResponseEntity<Object> handleSqlException(SQLException e){
		
		HttpStatus status = HttpStatus.BAD_REQUEST;
		
		ControllerException controllerException = new ControllerException(
				"SQL Exception",
				status,
                ZonedDateTime.now(ZoneId.of("Z"))
				);
		
		return new ResponseEntity<>(controllerException, status);
	}
}
