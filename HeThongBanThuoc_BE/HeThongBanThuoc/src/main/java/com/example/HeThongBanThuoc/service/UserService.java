package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.model.NguoiDung;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;

@Service
public class UserService {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Tìm user theo email
    public NguoiDung findByEmail(String email) {
        String sql = "SELECT * FROM NguoiDung WHERE Email = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NguoiDung user = new NguoiDung();
                    user.setMaNguoiDung(rs.getInt("MaNguoiDung"));
                    user.setMaVaiTro(rs.getInt("MaVaiTro"));
                    user.setEmail(rs.getString("Email"));
                    user.setSoDienThoai(rs.getString("SoDienThoai"));
                    user.setPasswordHash(rs.getString("PasswordHash"));
                    user.setHoTen(rs.getString("HoTen"));
                    user.setDiemTichLuy(rs.getInt("DiemTichLuy"));
                    user.setGioiTinh(rs.getString("GioiTinh"));
                    user.setAnhDaiDien(rs.getString("AnhDaiDien"));
                    user.setNgaySinh(rs.getDate("NgaySinh"));
                    return user;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm người dùng: " + e.getMessage(), e);
        }
        return null;
    }

    // Cập nhật mật khẩu
    public void updatePassword(NguoiDung user, String newPassword) {
        String sql = "UPDATE NguoiDung SET PasswordHash = ? WHERE MaNguoiDung = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, passwordEncoder.encode(newPassword));
            ps.setInt(2, user.getMaNguoiDung());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi cập nhật mật khẩu: " + e.getMessage(), e);
        }
    }

    // Kiểm tra mật khẩu hiện tại
    public boolean checkPassword(NguoiDung user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    // Lấy profile (ảnh đại diện + họ tên + giới tính + email + sđt + điểm + ngày sinh)
    public NguoiDung getUserProfile(Integer userId) {
        String sql = "SELECT HoTen, GioiTinh, Email, SoDienThoai, DiemTichLuy, AnhDaiDien, NgaySinh " +
                "FROM NguoiDung WHERE MaNguoiDung = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NguoiDung user = new NguoiDung();
                    user.setMaNguoiDung(userId);
                    user.setHoTen(rs.getString("HoTen"));
                    user.setGioiTinh(rs.getString("GioiTinh"));
                    user.setEmail(rs.getString("Email"));
                    user.setSoDienThoai(rs.getString("SoDienThoai"));
                    user.setDiemTichLuy(rs.getInt("DiemTichLuy"));
                    user.setAnhDaiDien(rs.getString("AnhDaiDien"));
                    user.setNgaySinh(rs.getDate("NgaySinh"));
                    return user;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy thông tin profile: " + e.getMessage(), e);
        }
        return null;
    }

    // Cập nhật profile người dùng
    public void updateUserProfile(NguoiDung user) {
        String sql = "UPDATE NguoiDung SET HoTen = ?, GioiTinh = ?, SoDienThoai = ?, AnhDaiDien = ?, NgaySinh = ? WHERE MaNguoiDung = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getHoTen());
            ps.setString(2, user.getGioiTinh());
            ps.setString(3, user.getSoDienThoai());
            ps.setString(4, user.getAnhDaiDien());
            if (user.getNgaySinh() != null) {
                ps.setDate(5, new java.sql.Date(user.getNgaySinh().getTime()));
            } else {
                ps.setDate(5, null);
            }
            ps.setInt(6, user.getMaNguoiDung());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi cập nhật profile: " + e.getMessage(), e);
        }
    }

    // LẤY ĐIỂM TÍCH LŨY CỦA NGƯỜI DÙNG
    public int getDiemHienCo(int maNguoiDung) {
        String sql = "SELECT DiemTichLuy FROM NguoiDung WHERE MaNguoiDung = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("DiemTichLuy");
                }
            }
        } catch (SQLException e) {
            System.err.println(" Lỗi khi lấy điểm hiện có: " + e.getMessage());
        }
        return 0;
    }

    // Cập nhật điểm tích lũy sau khi mua hàng
    public boolean capNhatDiemTichLuy(int maNguoiDung, int diemThayDoi) {
        String sql = "UPDATE NguoiDung SET DiemTichLuy = DiemTichLuy + ? WHERE MaNguoiDung = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, diemThayDoi);
            ps.setInt(2, maNguoiDung);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật điểm thưởng: " + e.getMessage());
            return false;
        }
    }
}

