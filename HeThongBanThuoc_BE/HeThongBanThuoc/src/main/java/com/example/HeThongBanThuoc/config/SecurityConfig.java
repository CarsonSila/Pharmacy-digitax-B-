package com.example.HeThongBanThuoc.config;

import com.example.HeThongBanThuoc.security.JwtAuthenticationFilter;
import com.example.HeThongBanThuoc.security.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          RateLimitFilter rateLimitFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF cho stateless REST API
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/user/chat/stream/**",
                                "/api/auth/**",
                                "/api/user/**",
                                "/api/admin/**",
                                "/api/banner/**",
                                "/api/sanpham/**",
                                "/api/danhmuc/**",
                                "/uploads/**",
                                "/api/admin/sanpham/uploads/**"
                        )
                )

                // FIX #14: Thêm security headers
                .headers(headers -> headers
                        // Chống clickjacking
                        .frameOptions(frame -> frame.deny())
                        // Chống MIME sniffing
                        .contentTypeOptions(ct -> {})
                        // Buộc HTTPS (HSTS) — chỉ có tác dụng khi dùng HTTPS
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true)
                        )
                        // Referrer policy
                        .referrerPolicy(ref -> ref
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )
                )

                // Phân quyền theo URL
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/banner/**",
                                "/api/sanpham/**",
                                "/api/danhmuc/**",
                                "/uploads/**",
                                "/api/admin/sanpham/uploads/**",
                                "/actuator/health",    // Docker / load balancer health check
                                "/error"
                        ).permitAll()

                        // SSE chat
                        .requestMatchers("/api/user/chat/stream/**").hasAuthority("ROLE_USER")

                        // User APIs
                        .requestMatchers("/api/user/**").hasAuthority("ROLE_USER")

                        // Admin APIs
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // Tất cả còn lại yêu cầu xác thực
                        .anyRequest().authenticated()
                )

                // Thêm JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Rate limiting filter — runs before JWT (order matters)
                .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class)

                // Tắt HTTP Basic
                .httpBasic(httpBasic -> httpBasic.disable())

                // Cho phép SSE giữ session
                .securityContext(securityContext -> securityContext.requireExplicitSave(false));

        return http.build();
    }
}
