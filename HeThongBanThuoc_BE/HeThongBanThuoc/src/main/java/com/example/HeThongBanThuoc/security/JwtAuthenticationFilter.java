package com.example.HeThongBanThuoc.security;

import com.example.HeThongBanThuoc.model.CustomUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    // Sử dụng JwtTokenProvider để kiểm tra và giải mã JWT
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // DataSource để truy vấn cơ sở dữ liệu
    @Autowired
    private DataSource dataSource;

    // Hàm quyết định xem request nào sẽ KHÔNG bị filter
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Endpoint cần xác thực JWT
        if (path.equals("/api/auth/change-password")) {
            return false;  // Filter sẽ chạy cho endpoint này
        }

        // Các endpoint khác có thể bỏ qua filter
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

    // Hàm chính của filter, chạy mỗi request (nếu shouldNotFilter trả về false)
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getServletPath();

        try {
            // Lấy header Authorization
            String header = request.getHeader("Authorization");

            // Kiểm tra xem header có tồn tại và bắt đầu bằng "Bearer "
            if (header != null && header.startsWith("Bearer ")) {
                // Lấy token thực tế bằng cách cắt bỏ "Bearer "
                String token = header.substring(7);

                // Kiểm tra JWT có hợp lệ không
                if (jwtTokenProvider.validateToken(token)) {

                    // Giải mã token lấy thông tin user
                    Integer userId = jwtTokenProvider.getUserIdFromToken(token);
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    String role = jwtTokenProvider.getRoleFromToken(token);

                    // Nếu tất cả thông tin cơ bản hợp lệ
                    if (userId != null && email != null && role != null) {

                        // Kiểm tra xem tài khoản có tồn tại trong database không
                        boolean userExists = false;
                        String checkSql = "SELECT COUNT(*) FROM NguoiDung WHERE MaNguoiDung = ?";
                        try (Connection conn = dataSource.getConnection();
                             PreparedStatement ps = conn.prepareStatement(checkSql)) {
                            ps.setInt(1, userId);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next() && rs.getInt(1) > 0) {
                                    userExists = true;
                                }
                            }
                        }

                        // Nếu tài khoản không tồn tại thì trả về 401
                        if (!userExists) {
                            SecurityContextHolder.clearContext(); // Xóa Authentication
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\":\"Tài khoản không tồn tại hoặc đã bị xóa\"}");
                            return; // Dừng filter chain
                        }

                        // Nếu user tồn tại, tạo CustomUserPrincipal
                        CustomUserPrincipal principal = new CustomUserPrincipal(userId, email);

                        // Tạo authority dựa trên role
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                        // Tạo Authentication object và set vào SecurityContext
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        principal,
                                        null,  // credentials đã được xác thực bằng JWT, không cần password
                                        Collections.singletonList(authority)
                                );

                        // Gán thêm thông tin chi tiết từ request
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // Log ra console để kiểm tra
                        System.out.println("Authenticated: " + email + " | Role: ROLE_" + role);
                    }

                } else {
                    // JWT không hợp lệ
                    System.err.println("Invalid JWT token for path: " + requestPath);
                }

            } else {
                // Không có header Authorization
                System.err.println("No Authorization header for path: " + requestPath);
            }

        } catch (Exception e) {
            // Xảy ra lỗi trong quá trình filter
            SecurityContextHolder.clearContext(); // Xóa Authentication
            System.err.println("JWT Filter Error on path " + requestPath + ": " + e.getMessage());
        }

        // Cho phép request tiếp tục đi xuống các filter tiếp theo
        filterChain.doFilter(request, response);
    }
}