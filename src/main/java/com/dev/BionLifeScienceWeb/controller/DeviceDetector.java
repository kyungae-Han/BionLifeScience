package com.dev.BionLifeScienceWeb.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.regex.Pattern;

@Configuration
public class DeviceDetector implements WebMvcConfigurer {

    // 일반적인 모바일/태블릿 패턴
    private static final Pattern MOBILE_UA = Pattern.compile(
        "(Mobile|Android|iPhone|iPad|iPod|Windows Phone|IEMobile|BlackBerry|Opera Mini|Mobi|Tablet)",
        Pattern.CASE_INSENSITIVE
    );

    private static final String COOKIE_VW = "vw";
    private static final int DESKTOP_MIN_VW = 1025; // 1025 이상은 무조건 PC

    private static Integer getCookieInt(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                try { return Integer.valueOf(c.getValue()); }
                catch (NumberFormatException ignore) {}
            }
        }
        return null;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {

            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
                // 0) UA/Client Hints
                String ua = req.getHeader("User-Agent");
                String chMobile   = req.getHeader("Sec-CH-UA-Mobile");
                String chPlatform = req.getHeader("Sec-CH-UA-Platform");

                boolean ipadByUaOs13 = ua != null
                        && ua.contains("Macintosh")
                        && ua.contains("Safari")
                        && ua.contains("Mobile/")       // macOS Safari에는 없음
                        && !ua.contains("Chrome");       // 크롬 데스크탑 오탐 방지

                boolean ipadClassic = ua != null && (ua.contains("iPad") || ua.contains("iPadOS") || ua.contains("iPad Pro"));

                // 2) Client Hints (가능하면 사용)
                boolean mobileByCH =
                        (chMobile != null && chMobile.contains("?1")) ||
                        (chPlatform != null && (chPlatform.contains("iOS") || chPlatform.contains("iPadOS") || chPlatform.contains("Android")));

                // 3) UA 기반 기본 탐지 + iPadOS 보완
                boolean mobileByUA = (ua != null && MOBILE_UA.matcher(ua).find()) || ipadByUaOs13 || ipadClassic;

                
                
                // ★ 폭 우선: 쿠키가 있으면 그것만으로 최종 결정 (<=1024 → MOBILE, >=1025 → PC)
                Integer vw =  getCookieInt(req, "vw");
                
                boolean isMobile = (vw != null)
                        ? (vw < DESKTOP_MIN_VW)
                        : (mobileByCH || mobileByUA);
                
                if (ua != null && ua.contains("iPad Pro")) {
                    isMobile = false;
                }

                
                String deviceType = isMobile ? "MOBILE" : "PC";

                // 요청/뷰 주입
                req.setAttribute("viewportWidth", vw);
                req.setAttribute("isMobile", isMobile);
                req.setAttribute("deviceType", deviceType);

                // 디버깅용 응답 헤더
                res.setHeader("X-Device-Type", deviceType);
                if (vw != null) res.setHeader("X-Viewport-Width", String.valueOf(vw));
                res.setHeader("X-VW-Policy", (vw != null ? "width-first" : "ua-fallback"));

                // (선택) 다음 요청부터 CH 받도록 힌트 요청
                 res.setHeader("Accept-CH", "Sec-CH-UA-Mobile, Sec-CH-UA-Platform");
                 res.addHeader("Vary", "Sec-CH-UA-Mobile, Sec-CH-UA-Platform");

                return true;
            }

            @Override
            public void postHandle(HttpServletRequest req, HttpServletResponse res, Object handler, ModelAndView mav) {
                if (mav == null) return;
                String viewName = mav.getViewName();
                if (viewName != null && viewName.startsWith("redirect:")) return;

                Object isMobile = req.getAttribute("isMobile");
                Object deviceType = req.getAttribute("deviceType");
                if (isMobile != null)   mav.addObject("isMobile", isMobile);
                if (deviceType != null) mav.addObject("deviceType", deviceType);
            }
        })
        .excludePathPatterns("/assets/**", "/static/**", "/public/**", "/webjars/**", "/favicon.*", "/robots.txt");
    }
}
