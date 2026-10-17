package com.example.HeThongBanThuoc.controller;

import com.example.HeThongBanThuoc.security.JwtTokenProvider;
import com.example.HeThongBanThuoc.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private DataSource dataSource;

    // API - Đăng ký tài khoản
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            // B1. Gọi service đăng ký (luôn gán role USER)
            String email = authService.register(
                    body.get("email"),
                    body.get("password"),
                    body.get("hoTen"),
                    null // không cho client chọn role
            );

            // B2. Trả phản hồi
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng ký thành công cho: " + email);
            response.put("email", email);
            response.put("role", "USER");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // B3. Nếu lỗi → trả Bad Request
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // API - Đăng nhập tài khoản (Updated với Refresh Token)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        try {
            // B1. Gọi service xử lý đăng nhập
            Map<String, Object> result = authService.login(email, password);

            // B2. Trả token, refreshToken, expiresIn, role và userId cho FE
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng nhập thành công");
            response.put("token", result.get("token"));
            response.put("refreshToken", result.get("refreshToken"));
            response.put("expiresIn", result.get("expiresIn"));
            response.put("role", result.get("role"));
            response.put("userId", result.get("userId"));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // B3. Sai thông tin → trả mã 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // API - Refresh Token (LẤY TOKEN MỚI KHI TOKEN CŨ SẮP HẾT HẠN)
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        try {
            // B1. Validate refresh token bằng JWT
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token không hợp lệ hoặc đã hết hạn"));
            }

            // B2. Kiểm tra xem có phải là refresh token không
            if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token này không phải là refresh token"));
            }

            // B3. Lấy email từ refresh token
            String email = jwtTokenProvider.getEmailFromToken(refreshToken);
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Không thể trích xuất email từ refresh token"));
            }

            // B4. Lấy thông tin user từ database
            Map<String, Object> user = authService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User không tồn tại"));
            }

            int userId = (int) user.get("userId");
            String role = (String) user.get("role");

            // B5. Kiểm tra refresh token có hợp lệ trong database không
            if (!authService.isRefreshTokenValid(userId, refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token đã bị thu hồi hoặc không hợp lệ"));
            }

            // B6. Tạo access token mới và refresh token mới
            String newAccessToken = jwtTokenProvider.generateToken(email, role, userId);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

            // B7. Lưu refresh token mới vào database
            authService.saveRefreshToken(userId, newRefreshToken);

            // B8. Trả về response
            Map<String, Object> response = new HashMap<>();
            response.put("token", newAccessToken);
            response.put("refreshToken", newRefreshToken);
            response.put("expiresIn", jwtTokenProvider.getAccessTokenExpirationInSeconds());

            System.out.println("Token refreshed successfully for: " + email);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Lỗi khi refresh token: " + e.getMessage()));
        }
    }

    // API - Kiểm tra token hợp lệ (xác thực phiên đăng nhập)
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // B1. Kiểm tra có header Authorization không
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Thiếu token hoặc định dạng không hợp lệ"));
            }

            // B2. Cắt chuỗi "Bearer " để lấy token
            String token = authHeader.substring(7);

            // B3. Xác minh token
            if (!jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Token không hợp lệ hoặc đã hết hạn"));
            }

            // B4. Lấy email từ token
            String email = jwtTokenProvider.getEmailFromToken(token);
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Không thể trích xuất email từ token"));
            }

            // B5. Kiểm tra người dùng có còn tồn tại trong DB không
            String sql = "SELECT COUNT(*) FROM NguoiDung WHERE Email = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message", "Tài khoản không còn tồn tại"));
                    }
                }
            }

            // B6. Nếu mọi thứ hợp lệ
            return ResponseEntity.ok(Map.of("message", "Token hợp lệ", "email", email));

        } catch (Exception e) {
            // B7. Nếu có lỗi trong quá trình xác thực
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Token không hợp lệ hoặc người dùng không tồn tại"));
        }
    }

    // API - Đăng xuất (Thu hồi refresh token)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Thiếu token"));
            }

            String token = authHeader.substring(7);

            // Lấy userId từ token
            Integer userId = jwtTokenProvider.getUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token không hợp lệ"));
            }

            // Thu hồi refresh token
            authService.revokeRefreshToken(userId);

            return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi đăng xuất: " + e.getMessage()));
        }
    }
}