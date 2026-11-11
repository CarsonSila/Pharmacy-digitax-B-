-- Migration V1: Thêm cột AttemptCount vào bảng OTP
-- FIX #8: Hỗ trợ giới hạn số lần thử OTP

ALTER TABLE OTP
    ADD COLUMN IF NOT EXISTS AttemptCount INT NOT NULL DEFAULT 0 COMMENT 'Số lần thử OTP';

-- Đảm bảo index hiệu quả cho truy vấn validate OTP
CREATE INDEX IF NOT EXISTS idx_otp_user_status ON OTP (MaNguoiDung, TrangThai);
