package com.example.HeThongBanThuoc.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FIX #7: Rate limiting cho các endpoint nhạy cảm (login, OTP, forgot-password).
 *
 * Sử dụng sliding-window đơn giản trong bộ nhớ.
 * Cho production nên dùng Redis (spring-boot-starter-data-redis) để shared giữa nhiều instance.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    @Value("${app.rate-limit.login.capacity:5}")
    private int loginCapacity;

    @Value("${app.rate-limit.login.refill-seconds:60}")
    private long refillSeconds;

    // Map: IP + path → (attempt_count, window_start_ms)
    private final Map<String, long[]> attempts = new ConcurrentHashMap<>();

    private static final String[] RATE_LIMITED_PATHS = {
        "/api/auth/login",
        "/api/auth/forgot-password",
        "/api/auth/verify-otp",
        "/api/auth/reset-password"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        for (String limited : RATE_LIMITED_PATHS) {
            if (path.startsWith(limited)) return false;
        }
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String key = clientIp + ":" + request.getServletPath();

        long now = System.currentTimeMillis();
        long windowMs = refillSeconds * 1000L;

        long[] state = attempts.compute(key, (k, existing) -> {
            if (existing == null || (now - existing[1]) > windowMs) {
                // Nova janela
                return new long[]{1, now};
            }
            existing[0]++;
            return existing;
        });

        long count = state[0];

        if (count > loginCapacity) {
            long waitSeconds = refillSeconds - ((now - state[1]) / 1000);
            log.warn("Rate limit exceeded for IP={} path={} count={}", clientIp, request.getServletPath(), count);
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Retry-After", String.valueOf(Math.max(0, waitSeconds)));
            response.getWriter().write(
                "{\"error\":\"Quá nhiều yêu cầu. Vui lòng thử lại sau " + Math.max(0, waitSeconds) + " giây.\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        // Hỗ trợ proxy/load balancer
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
