package com.example.HeThongBanThuoc.config;

import com.example.HeThongBanThuoc.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true) // Cho phép dùng @PreAuthorize/@PostAuthorize trên method
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Bean PasswordEncoder dùng mã hóa mật khẩu người dùng (BCrypt, có salt tự động)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cấu hình SecurityFilterChain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF cho các API stateless
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
                // Phân quyền theo URL
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — không yêu cầu token
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/banner/**",
                                "/api/sanpham/**",
                                "/api/danhmuc/**",
                                "/uploads/**",
                                "/api/admin/sanpham/uploads/**",
                                "/error"
                        ).permitAll()

                        // SSE chat
                        .requestMatchers("/api/user/chat/stream/**").hasAuthority("ROLE_USER")

                        // Các API người dùng
                        .requestMatchers("/api/user/**").hasAuthority("ROLE_USER")

                        // Các API admin
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // Các request còn lại yêu cầu xác thực
                        .anyRequest().authenticated()
                )
                // Thêm JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Tắt HTTP Basic
                .httpBasic(httpBasic -> httpBasic.disable())

                // Cho phép SSE giữ session
                .securityContext(securityContext -> securityContext.requireExplicitSave(false));

        return http.build();
    }
}
