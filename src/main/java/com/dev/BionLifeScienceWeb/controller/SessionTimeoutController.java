package com.dev.BionLifeScienceWeb.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SessionTimeoutController {

    @GetMapping("/session-timeout")
    @ResponseBody
    public String sessionTimeout(Authentication authentication) {
        if (authentication != null &&
            authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            // 관리자 세션 만료
            return "<script>"
                 + "alert('관리자 세션이 만료되었습니다. 로그인 페이지로 이동합니다.');"
                 + "location.href='/memberLoginForm'"
                 + "</script>";
        } else {
            // 일반 사용자 세션 만료
            return "<script>"
                 + "alert('세션이 만료되었습니다. 메인 페이지로 이동합니다.');"
                 + "location.href='/'"
                 + "</script>";
        }
    }
}
