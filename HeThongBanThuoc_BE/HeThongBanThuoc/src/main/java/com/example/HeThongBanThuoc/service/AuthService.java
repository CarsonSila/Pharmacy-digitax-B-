package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // FIX #13: Đã xoá @PostConstruct generateAdminPasswordHash() — nó log thông tin
    // mã hoá nhạy cảm mỗi lần khởi động. Dùng lệnh một lần để hash mật khẩu admin.

    // ĐĂNG KÝ NGƯỜI DÙNG
    public String register(String email, String rawPassword, String hoTen, String maVaiTroStr) {
        // Mặc định USER, không cho client chọn ADMIN
        int maVaiTro = 2;

        if (maVaiTroStr != null && !maVaiTroStr.isBlank()) {
            try {
                int temp = Integer.parseInt(maVaiTroStr.trim());
                if (temp == 1) {
                    throw new RuntimeException("Bạn không thể tự đăng ký tài khoản ADMIN!");
                }
            } catch (NumberFormatException e) {
                log.warn("Giá trị MaVaiTro không hợp lệ, gán mặc định USER.");
            }
        }

        // Kiểm tra email trùng
        String checkSql = "SELECT 1 FROM NguoiDung WHERE Email = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setString(1, email);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    throw new RuntimeException("Email đã được sử dụng.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi kiểm tra email: " + e.getMessage(), e);
        }

        // Thêm mới người dùng
        String sql = "INSERT INTO NguoiDung (MaVaiTro, Email, PasswordHash, HoTen, TrangThai, NgayTao) " +
                     "VALUES (?, ?, ?, ?, 1, NOW())";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, maVaiTro);
            ps.setString(2, email);
            ps.setString(3, passwordEncoder.encode(rawPassword));
            ps.setString(4, hoTen);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    log.info("Dang ky thanh cong: {} (Vai tro USER)", email);
                    return email;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm người dùng: " + e.getMessage(), e);
        }

        return email;
    }

    // ĐĂNG NHẬP NGƯỜI DÙNG
    public Map<String, Object> login(String email, String rawPassword) {
        // FIX #6: thêm AND TrangThai = 1 để chặn tài khoản bị khóa đăng nhập
        String sql = "SELECT MaNguoiDung, PasswordHash, MaVaiTro FROM NguoiDung WHERE Email = ? AND TrangThai = 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // Thông báo chung — không tiết lộ tài khoản có tồn tại hay không
                    throw new RuntimeException("Sai email hoặc mật khẩu");
                }

                int userId = rs.getInt("MaNguoiDung");
                String hash = rs.getString("PasswordHash");
                Integer maVaiTroObj = (Integer) rs.getObject("MaVaiTro");
                int maVaiTro = (maVaiTroObj != null) ? maVaiTroObj : 2;

                if (!passwordEncoder.matches(rawPassword, hash)) {
                    throw new RuntimeException("Sai email hoặc mật khẩu");
                }

                String roleName = (maVaiTro == 1) ? "ADMIN" : "USER";

                String accessToken  = jwtTokenProvider.generateToken(email, roleName, userId);
                String refreshToken = jwtTokenProvider.generateRefreshToken(email);

                saveRefreshToken(userId, refreshToken);

                log.info("Login successful for: {} | Role: {}", email, roleName);

                Map<String, Object> result = new HashMap<>();
                result.put("token", accessToken);
                result.put("refreshToken", refreshToken);
                result.put("expiresIn", jwtTokenProvider.getAccessTokenExpirationInSeconds());
                result.put("role", roleName);
                result.put("userId", userId);

                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi đăng nhập: " + e.getMessage(), e);
        }
    }

    // LƯU REFRESH TOKEN VÀO DATABASE
    public void saveRefreshToken(int userId, String refreshToken) {
        String sql = "UPDATE NguoiDung SET RefreshToken = ?, " +
                     "RefreshTokenExpiry = DATE_ADD(NOW(), INTERVAL 7 DAY) WHERE MaNguoiDung = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, refreshToken);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Loi khi luu refresh token userId={}: {}", userId, e.getMessage());
        }
    }

    // KIỂM TRA REFRESH TOKEN HỢP LỆ
    public boolean isRefreshTokenValid(int userId, String refreshToken) {
        String sql = "SELECT RefreshToken, RefreshTokenExpiry FROM NguoiDung WHERE MaNguoiDung = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedToken = rs.getString("RefreshToken");
                    Timestamp expiry   = rs.getTimestamp("RefreshTokenExpiry");
                    if (storedToken != null && storedToken.equals(refreshToken)) {
                        if (expiry != null && expiry.after(new Timestamp(System.currentTimeMillis()))) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Loi kiem tra refresh token userId={}: {}", userId, e.getMessage());
        }
        return false;
    }

    // XÓA REFRESH TOKEN (KHI LOGOUT)
    public void revokeRefreshToken(int userId) {
        String sql = "UPDATE NguoiDung SET RefreshToken = NULL, RefreshTokenExpiry = NULL " +
                     "WHERE MaNguoiDung = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            log.info("Refresh token revoked for userId: {}", userId);
        } catch (SQLException e) {
            log.error("Loi xoa refresh token userId={}: {}", userId, e.getMessage());
        }
    }

    // LẤY THÔNG TIN USER TỪ EMAIL
    public Map<String, Object> getUserByEmail(String email) {
        String sql = "SELECT MaNguoiDung, MaVaiTro, HoTen FROM NguoiDung WHERE Email = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("userId", rs.getInt("MaNguoiDung"));
                    int maVaiTro = rs.getInt("MaVaiTro");
                    user.put("role", (maVaiTro == 1) ? "ADMIN" : "USER");
                    user.put("hoTen", rs.getString("HoTen"));
                    return user;
                }
            }
        } catch (SQLException e) {
            log.error("Loi lay thong tin user email={}: {}", email, e.getMessage());
        }
        return null;
    }
}
