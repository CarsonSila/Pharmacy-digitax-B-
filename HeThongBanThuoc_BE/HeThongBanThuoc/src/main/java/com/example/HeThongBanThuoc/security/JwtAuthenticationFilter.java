package com.example.HeThongBanThuoc.security;

import com.example.HeThongBanThuoc.model.CustomUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private DataSource dataSource;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Endpoint đổi mật khẩu cần xác thực JWT
        if (path.equals("/api/auth/change-password")) {
            return false;
        }

        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/refresh") ||
                path.startsWith("/api/auth/forgot-password") ||
                path.startsWith("/api/auth/verify-otp") ||
                path.startsWith("/api/auth/reset-password") ||
                path.startsWith("/api/sanpham") ||
                path.startsWith("/api/danhmuc") ||
                path.startsWith("/api/banner");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getServletPath();

        try {
            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                if (jwtTokenProvider.validateToken(token)) {
                    Integer userId = jwtTokenProvider.getUserIdFromToken(token);
                    String email   = jwtTokenProvider.getEmailFromToken(token);
                    String role    = jwtTokenProvider.getRoleFromToken(token);

                    if (userId != null && email != null && role != null) {

                        // FIX #6: kiểm tra user tồn tại VÀ chưa bị khoá (TrangThai = 1)
                        boolean userActive = false;
                        String checkSql = "SELECT COUNT(*) FROM NguoiDung WHERE MaNguoiDung = ? AND TrangThai = 1";
                        try (Connection conn = dataSource.getConnection();
                             PreparedStatement ps = conn.prepareStatement(checkSql)) {
                            ps.setInt(1, userId);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next() && rs.getInt(1) > 0) {
                                    userActive = true;
                                }
                            }
                        }

                        if (!userActive) {
                            SecurityContextHolder.clearContext();
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"Tài khoản không tồn tại hoặc đã bị khóa\"}");
                            return;
                        }

                        CustomUserPrincipal principal = new CustomUserPrincipal(userId, email);
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        principal, null, Collections.singletonList(authority));
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("Authenticated: {} | Role: ROLE_{}", email, role);
                    }

                } else {
                    log.warn("Invalid JWT token for path: {}", requestPath);
                }

            } else {
                log.debug("No Authorization header for path: {}", requestPath);
            }

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.error("JWT Filter error on path {}: {}", requestPath, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
