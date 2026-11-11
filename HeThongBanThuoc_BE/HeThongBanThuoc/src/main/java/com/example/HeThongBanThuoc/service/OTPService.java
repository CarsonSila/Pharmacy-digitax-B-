package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.model.NguoiDung;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

@Service
public class OTPService {

    private static final Logger log = LoggerFactory.getLogger(OTPService.class);

    // FIX #8: Dùng SecureRandom thay vì java.util.Random (không đủ entropy)
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // FIX #8: Giới hạn số lần thử OTP
    private static final int MAX_OTP_ATTEMPTS = 3;
    // OTP hết hạn sau 5 phút
    private static final int OTP_EXPIRY_MINUTES = 5;

    @Autowired
    private DataSource dataSource;

    /**
     * Sinh OTP mới và lưu vào DB.
     * Xóa OTP cũ của user trước khi tạo mới.
     */
    public String generateOTP(NguoiDung nguoiDung) {
        // 6 chữ số, dùng SecureRandom
        String otpCode = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

        String deleteSql = "DELETE FROM OTP WHERE MaNguoiDung = ?";
        // FIX #8: thêm cột AttemptCount để theo dõi số lần thử
        String insertSql = "INSERT INTO OTP (MaNguoiDung, OtpCode, ExpiryTime, TrangThai, AttemptCount, NgayTao) " +
                           "VALUES (?, ?, ?, 1, 0, ?)";

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, OTP_EXPIRY_MINUTES);
        Date expiryTime = cal.getTime();
        Date now = new Date();

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement deletePs = conn.prepareStatement(deleteSql);
                 PreparedStatement insertPs = conn.prepareStatement(insertSql)) {

                deletePs.setInt(1, nguoiDung.getMaNguoiDung());
                deletePs.executeUpdate();

                insertPs.setInt(1, nguoiDung.getMaNguoiDung());
                insertPs.setString(2, otpCode);
                insertPs.setTimestamp(3, new java.sql.Timestamp(expiryTime.getTime()));
                insertPs.setTimestamp(4, new java.sql.Timestamp(now.getTime()));
                insertPs.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tạo OTP: " + e.getMessage(), e);
        }

        return otpCode;
    }

    /**
     * Kiểm tra OTP hợp lệ.
     * FIX #8: Kiểm tra thời gian hết hạn VÀ số lần thử.
     */
    public boolean validateOTP(NguoiDung nguoiDung, String otpCode) {
        String sql = "SELECT MaOTP, ExpiryTime, AttemptCount FROM OTP " +
                     "WHERE MaNguoiDung = ? AND OtpCode = ? AND TrangThai = 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, nguoiDung.getMaNguoiDung());
            ps.setString(2, otpCode);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int maOTP        = rs.getInt("MaOTP");
                    Date expiryTime  = rs.getTimestamp("ExpiryTime");
                    int attemptCount = rs.getInt("AttemptCount");

                    // FIX #8: Kiểm tra giới hạn thử
                    if (attemptCount >= MAX_OTP_ATTEMPTS) {
                        log.warn("OTP exceeded max attempts for user {}", nguoiDung.getMaNguoiDung());
                        // Vô hiệu hoá OTP này
                        invalidateOTP(conn, maOTP);
                        throw new RuntimeException("OTP đã bị vô hiệu hoá do thử quá " + MAX_OTP_ATTEMPTS + " lần.");
                    }

                    // Tăng bộ đếm
                    incrementAttempt(conn, maOTP);

                    // Kiểm tra hết hạn
                    if (expiryTime.before(new Date())) {
                        throw new RuntimeException("OTP đã hết hạn.");
                    }

                    // Thành công — đánh dấu đã dùng
                    invalidateOTP(conn, maOTP);
                    return true;
                }
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi kiểm tra OTP: " + e.getMessage(), e);
        }

        return false;
    }

    private void invalidateOTP(Connection conn, int maOTP) throws SQLException {
        String sql = "UPDATE OTP SET TrangThai = 0 WHERE MaOTP = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maOTP);
            ps.executeUpdate();
        }
    }

    private void incrementAttempt(Connection conn, int maOTP) throws SQLException {
        String sql = "UPDATE OTP SET AttemptCount = AttemptCount + 1 WHERE MaOTP = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maOTP);
            ps.executeUpdate();
        }
    }
}
