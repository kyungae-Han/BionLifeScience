package com.dev.BionLifeScienceWeb.filter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 스미싱/스팸 차단을 위한 IP 차단 필터
 * - 고정 차단 IP 목록 관리
 * - /clientInsert 엔드포인트에 적용
 */
@Component
public class BlockedIpFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(BlockedIpFilter.class);

    // 차단 대상 IP 목록 (런타임에 추가/제거 가능)
    private final Set<String> blockedIps = ConcurrentHashMap.newKeySet();

    // 차단 대상 엔드포인트
    private static final Set<String> PROTECTED_PATHS = Set.of("/clientInsert");

    public BlockedIpFilter() {
        // 2026-03-31 스미싱 사건 차단 IP
        blockedIps.add("122.137.220.51");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        if (PROTECTED_PATHS.contains(path)) {
            String clientIp = extractClientIp(httpRequest);

            if (blockedIps.contains(clientIp)) {
                log.warn("[IP 차단] 차단된 IP 접근 시도: {} -> {}", clientIp, path);
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.setContentType("text/html; charset=UTF-8");
                httpResponse.getWriter().write("""
                    <script>
                        alert('비정상적인 접근이 감지되어 차단되었습니다.');
                        window.location.href = '/index';
                    </script>
                """);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * 클라이언트 실제 IP 추출 (프록시/로드밸런서 환경 대응)
     */
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
                // X-Forwarded-For는 쉼표로 구분된 IP 목록일 수 있음 (첫 번째가 원본)
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    /** 런타임에 IP 차단 추가 */
    public void blockIp(String ip) {
        blockedIps.add(ip);
        log.info("[IP 차단] IP 추가: {}", ip);
    }

    /** 런타임에 IP 차단 해제 */
    public void unblockIp(String ip) {
        blockedIps.remove(ip);
        log.info("[IP 차단] IP 해제: {}", ip);
    }

    /** 현재 차단 IP 목록 조회 */
    public Set<String> getBlockedIps() {
        return Set.copyOf(blockedIps);
    }
}
