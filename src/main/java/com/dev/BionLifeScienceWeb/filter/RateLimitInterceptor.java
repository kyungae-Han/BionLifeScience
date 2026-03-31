package com.dev.BionLifeScienceWeb.filter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 고객문의 폼 요청 속도 제한 인터셉터
 * - 동일 IP에서 일정 시간 내 반복 요청 차단
 * - 스미싱/스팸 대량 발송 방지
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    // IP별 요청 횟수 추적
    private final ConcurrentHashMap<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

    // 제한 설정: 10분 내 최대 5회 요청 허용
    private static final int MAX_REQUESTS = 5;
    private static final long TIME_WINDOW_MS = 10 * 60 * 1000L; // 10분

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String clientIp = extractClientIp(request);
        long now = System.currentTimeMillis();

        RequestInfo info = requestCounts.compute(clientIp, (ip, existing) -> {
            if (existing == null || (now - existing.windowStart) > TIME_WINDOW_MS) {
                return new RequestInfo(now);
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (info.count.get() > MAX_REQUESTS) {
            log.warn("[속도 제한] IP {}에서 {}분 내 {}회 초과 요청 감지", clientIp, TIME_WINDOW_MS / 60000, info.count.get());
            response.setContentType("text/html; charset=UTF-8");
            response.setStatus(429);
            response.getWriter().write("""
                <script>
                    alert('잠시 후 다시 시도해 주세요. 짧은 시간 내 너무 많은 요청이 감지되었습니다.');
                    window.location.href = '/index';
                </script>
            """);
            return false;
        }

        return true;
    }

    private String extractClientIp(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private static class RequestInfo {
        final long windowStart;
        final AtomicInteger count;

        RequestInfo(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(1);
        }
    }
}
