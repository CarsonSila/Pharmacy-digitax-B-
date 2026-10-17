package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.model.NguoiDung;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@Service
public class OTPService {
    @Autowired
    private DataSource dataSource;

    // Sinh OTP và lưu vào DB
    public String generateOTP(NguoiDung nguoiDung) {
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        String deleteSql = "DELETE FROM OTP WHERE MaNguoiDung = ?";
        String insertSql = "INSERT INTO OTP (MaNguoiDung, OtpCode, ExpiryTime, TrangThai, NgayTao) VALUES (?, ?, ?, 1, ?)";

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 5); // OTP 5 phút
        Date expiryTime = cal.getTime();
        Date now = new Date();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement deletePs = conn.prepareStatement(deleteSql);
             PreparedStatement insertPs = conn.prepareStatement(insertSql)) {

            deletePs.setInt(1, nguoiDung.getMaNguoiDung());
            deletePs.executeUpdate();

            insertPs.setInt(1, nguoiDung.getMaNguoiDung());
            insertPs.setString(2, otpCode);
            insertPs.setTimestamp(3, new java.sql.Timestamp(expiryTime.getTime()));
            insertPs.setTimestamp(4, new java.sql.Timestamp(now.getTime()));
            insertPs.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tạo OTP: " + e.getMessage(), e);
        }

        return otpCode;
    }

    // Kiểm tra OTP
    public boolean validateOTP(NguoiDung nguoiDung, String otpCode) {
        String sql = "SELECT * FROM OTP WHERE MaNguoiDung = ? AND OtpCode = ? AND TrangThai = 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, nguoiDung.getMaNguoiDung());
            ps.setString(2, otpCode);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date expiryTime = rs.getTimestamp("ExpiryTime");
                    if (expiryTime.after(new Date())) {
                        // đánh dấu đã dùng
                        String updateSql = "UPDATE OTP SET TrangThai = 0 WHERE MaOTP = ?";
                        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                            updatePs.setInt(1, rs.getInt("MaOTP"));
                            updatePs.executeUpdate();
                        }
                        return true;
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi kiểm tra OTP: " + e.getMessage(), e);
        }

        return false;
    }
}
