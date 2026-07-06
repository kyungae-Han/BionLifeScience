package com.dev.BionLifeScienceWeb.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.dev.BionLifeScienceWeb.filter.RateLimitInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final RateLimitInterceptor rateLimitInterceptor;

  @Value("${spring.upload.path}")
  private String uploadPath;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/upload/**")
            .addResourceLocations("file:" + uploadPath + "/")
            .setCacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic());
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 고객문의 폼에만 속도 제한 적용
    registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/clientInsert");
  }
}
