package com.dev.BionLifeScienceWeb.handler;

import java.io.IOException;
import java.net.URLEncoder;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

@Component
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler{
	
	@SneakyThrows
	@Override
    public void onAuthenticationFailure(
    		HttpServletRequest request, 
    		HttpServletResponse response,
            AuthenticationException exception
            ) throws IOException, ServletException {
        
		String errorMessage = "잘못된 아이디 혹은 패스워드 입니다.";
        if (exception instanceof BadCredentialsException) {
            errorMessage = "비밀번호가 잘못 되었습니다.";
        } else if (exception instanceof InternalAuthenticationServiceException) {
            errorMessage = "InternalAuthenticationServiceException";
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = "없는 계정 입니다.";
        } else if (exception instanceof AuthenticationCredentialsNotFoundException) {
            errorMessage = "AuthenticationCredentialsNotFoundException";
        } else {
        	System.out.println(exception);
            errorMessage = "알 수 없는 이유로 로그인에 실패하였습니다 관리자에게 문의하세요.";
        }
        errorMessage = URLEncoder.encode(errorMessage, "UTF-8");
        setDefaultFailureUrl("/memberLoginForm?error=true&exception="+errorMessage);
 
        super.onAuthenticationFailure(request, response, exception);
		
		
    }
}
