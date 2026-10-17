package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    // Tự động sinh mật khẩu mã hóa cho admin -> chỉ thực hiện cho tài khoản đầu tiên của hệ thống
    @PostConstruct
    public void generateAdminPasswordHash() {
        String rawPassword = "admin01"; // mật khẩu công khai
        String encoded = passwordEncoder.encode(rawPassword);
        System.out.println("Mật khẩu mã hóa cho ADMIN là: " + encoded);
    }

    //  ĐĂNG KÝ NGƯỜI DÙNG
    public String register(String email, String rawPassword, String hoTen, String maVaiTroStr) {
        int maVaiTro = 2; // Mặc định USER

        // Chặn người cố tình chọn ADMIN
        if (maVaiTroStr != null && !maVaiTroStr.isBlank()) {
            try {
                int temp = Integer.parseInt(maVaiTroStr.trim());
                if (temp == 1) {
                    throw new RuntimeException("Bạn không thể tự đăng ký tài khoản ADMIN!");
                }
            } catch (NumberFormatException e) {
                System.err.println("Giá trị MaVaiTro không hợp lệ, gán mặc định USER.");
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
        String sql = """
                INSERT INTO NguoiDung (MaVaiTro, Email, PasswordHash, HoTen, TrangThai, NgayTao)
                VALUES (?, ?, ?, ?, 1, NOW())
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, maVaiTro);
            ps.setString(2, email);
            ps.setString(3, passwordEncoder.encode(rawPassword));
            ps.setString(4, hoTen);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    System.out.println("Đăng ký thành công: " + email + " (Vai trò USER)");
                    return email;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm người dùng: " + e.getMessage(), e);
        }

        return email;
    }

    // ĐĂNG NHẬP NGƯỜI DÙNG - Updated với Refresh Token
    public Map<String, Object> login(String email, String rawPassword) {
        String sql = "SELECT MaNguoiDung, PasswordHash, MaVaiTro FROM NguoiDung WHERE Email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException("Sai email hoặc mật khẩu");
                }

                int userId = rs.getInt("MaNguoiDung");
                String hash = rs.getString("PasswordHash");

                Integer maVaiTroObj = (Integer) rs.getObject("MaVaiTro");
                int maVaiTro = (maVaiTroObj != null) ? maVaiTroObj.intValue() : 2;

                if (!passwordEncoder.matches(rawPassword, hash)) {
                    throw new RuntimeException("Sai email hoặc mật khẩu");
                }

                // Xác định vai trò
                String roleName;
                if (maVaiTro == 1) {
                    roleName = "ADMIN";
                } else if (maVaiTro == 2) {
                    roleName = "USER";
                } else {
                    roleName = "USER";
                }

                // Sinh Access Token và Refresh Token
                String accessToken = jwtTokenProvider.generateToken(email, roleName, userId);
                String refreshToken = jwtTokenProvider.generateRefreshToken(email);

                // Lưu refresh token vào database
                saveRefreshToken(userId, refreshToken);

                System.out.println("Token tạo cho " + email + " | Role: " + roleName);

                // Trả kết quả về dạng Map
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
        String sql = "UPDATE NguoiDung SET RefreshToken = ?, RefreshTokenExpiry = DATE_ADD(NOW(), INTERVAL 7 DAY) WHERE MaNguoiDung = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, refreshToken);
            ps.setInt(2, userId);
            ps.executeUpdate();

            System.out.println("Refresh token saved for userId: " + userId);

        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu refresh token: " + e.getMessage());
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
                    Timestamp expiry = rs.getTimestamp("RefreshTokenExpiry");

                    // Kiểm tra token trùng khớp và chưa hết hạn
                    if (storedToken != null && storedToken.equals(refreshToken)) {
                        if (expiry != null && expiry.after(new Timestamp(System.currentTimeMillis()))) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra refresh token: " + e.getMessage());
        }

        return false;
    }

    // XÓA REFRESH TOKEN (KHI LOGOUT)
    public void revokeRefreshToken(int userId) {
        String sql = "UPDATE NguoiDung SET RefreshToken = NULL, RefreshTokenExpiry = NULL WHERE MaNguoiDung = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();

            System.out.println("Refresh token revoked for userId: " + userId);

        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa refresh token: " + e.getMessage());
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
                    String roleName = (maVaiTro == 1) ? "ADMIN" : "USER";
                    user.put("role", roleName);
                    user.put("hoTen", rs.getString("HoTen"));

                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy thông tin user: " + e.getMessage());
        }

        return null;
    }
}