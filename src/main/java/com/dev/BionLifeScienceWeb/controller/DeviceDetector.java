package com.dev.BionLifeScienceWeb.controller;

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

 private static final Pattern MOBILE_UA = Pattern.compile(
         "(Mobile|Android|iPhone|iPad|iPod|Windows Phone|IEMobile|BlackBerry|Opera Mini|Mobi|Tablet)",
         Pattern.CASE_INSENSITIVE
 );

 @Override
 public void addInterceptors(InterceptorRegistry registry) {
     registry.addInterceptor(new HandlerInterceptor() {

    	 @Override
    	 public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
    	     String ua = req.getHeader("User-Agent");
    	     
    	     // iPad 명시적 감지 로직 추가
    	     boolean isIpad = ua != null && (
 	    		    ua.contains("iPad") ||
 	    		     ua.contains("iPad Pro") || 
 	    		    (ua.contains("Macintosh") && 
 	    		     ua.contains("Safari") && 
 	    		     !ua.contains("Chrome"))
 	    		);
    	     
    	    
    	                      
    	     boolean isMobile = ua != null && (MOBILE_UA.matcher(ua).find() || isIpad);
    	     String deviceType = isMobile ? "MOBILE" : "PC";

    	     // 요청 범위에 주입
    	     req.setAttribute("isMobile", isMobile);
    	     req.setAttribute("deviceType", deviceType);

    	     // 선택: 확인용 헤더
    	     res.setHeader("X-Device-Type", deviceType);
    	     return true;
    	 }

         @Override
         public void postHandle(HttpServletRequest req, HttpServletResponse res, Object handler, ModelAndView mav) {
             if (mav == null) return;                          // REST 등
             String viewName = mav.getViewName();
             if (viewName != null && viewName.startsWith("redirect:")) return;

             Object isMobile = req.getAttribute("isMobile");
             Object deviceType = req.getAttribute("deviceType");
             if (isMobile != null)   mav.addObject("isMobile", isMobile);
             if (deviceType != null) mav.addObject("deviceType", deviceType);
         }
         
     })
     // 정적 리소스 등 불필요 경로 제외(프로젝트에 맞게 조정)
     .excludePathPatterns("/assets/**", "/static/**", "/public/**", "/webjars/**", "/favicon.*", "/robots.txt");
 }
}