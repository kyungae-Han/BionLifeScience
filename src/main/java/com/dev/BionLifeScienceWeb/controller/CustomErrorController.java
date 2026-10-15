package com.dev.BionLifeScienceWeb.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CustomErrorController implements ErrorController{

	// 에러 VIEW 공통 PATH
	private String VIEW_PATH = "/error/";
	
	// GET 만 받으면 POST 요청이 실패했을 때 스프링이 원래 메서드를 유지한 채
	// /error 로 넘기므로 405 가 나고 진짜 오류가 가려진다. 모든 메서드를 받는다.
	@RequestMapping("/error")
	public String handleError(
			HttpServletRequest request,
			Model model) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		if(status != null) {
			int statusCode = Integer.valueOf(status.toString());
			
			if(statusCode == HttpStatus.NOT_FOUND.value()) {
				return VIEW_PATH+"404";
			}else if(statusCode == 500){
				return VIEW_PATH+"500";
			}else {
				return VIEW_PATH+"403";
			}
		}
		return "error";
	}
	
}
