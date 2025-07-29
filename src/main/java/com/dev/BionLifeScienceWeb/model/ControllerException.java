package com.dev.BionLifeScienceWeb.model;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ControllerException {

	private final String message;
	private final HttpStatus httpStatus;
	private final ZonedDateTime timeStamp;
}
