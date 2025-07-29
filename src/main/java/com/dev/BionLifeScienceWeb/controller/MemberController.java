package com.dev.BionLifeScienceWeb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class MemberController {
	
	@GetMapping("/memberLoginForm")
	public String memberLoginForm() {
		log.info("MEMBER LOGIN FORM");
		return "admin/login";
	}

}
