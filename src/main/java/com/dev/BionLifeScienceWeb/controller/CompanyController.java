package com.dev.BionLifeScienceWeb.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front/company")
public class CompanyController {

    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "front/company/privacy-policy";
    }

    @GetMapping("/privacy-policy/iframe")
    public String privacyPolicyIframe(HttpServletResponse res) {
        res.setHeader("X-Frame-Options", "SAMEORIGIN");
        res.setHeader("Content-Security-Policy", "frame-ancestors 'self'");
        return "front/company/privacy-policy";
    }
    
}
