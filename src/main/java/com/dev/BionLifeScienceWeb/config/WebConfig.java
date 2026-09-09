package com.dev.BionLifeScienceWeb.config;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

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

  /**
   * 언어 선택을 쿠키에 담는다. 세션에 담으면 브라우저를 닫는 순간 잊어버려서
   * 다음 방문에 다시 한글로 돌아온다.
   */
  @Bean
  LocaleResolver localeResolver() {
    CookieLocaleResolver resolver = new CookieLocaleResolver("BION_LANG");
    resolver.setDefaultLocale(Locale.KOREAN);
    resolver.setCookiePath("/");
    resolver.setCookieMaxAge(Duration.ofDays(365));
    return resolver;
  }

  /** 주소에 ?lang=en 이 붙으면 언어를 바꾼다 */
  @Bean
  LocaleChangeInterceptor localeChangeInterceptor() {
    LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
    interceptor.setParamName("lang");
    return interceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 고객문의 폼에만 속도 제한 적용
    registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/clientInsert");

    // 언어 전환은 모든 화면에서 동작해야 한다
    registry.addInterceptor(localeChangeInterceptor());
  }
}
