package com.example.HeThongBanThuoc.model;

import java.util.Date;

public class OTP {
    private Integer MaOTP;
    private Integer MaNguoiDung;
    private String OtpCode;
    private Date ExpiryTime;
    private Integer TrangThai; // 1: chưa dùng, 0: đã dùng
    private Date NgayTao;

    // Getters & Setters
    public Integer getMaOTP() { return MaOTP; }
    public void setMaOTP(Integer maOTP) { MaOTP = maOTP; }

    public Integer getMaNguoiDung() { return MaNguoiDung; }
    public void setMaNguoiDung(Integer maNguoiDung) { MaNguoiDung = maNguoiDung; }

    public String getOtpCode() { return OtpCode; }
    public void setOtpCode(String otpCode) { OtpCode = otpCode; }

    public Date getExpiryTime() { return ExpiryTime; }
    public void setExpiryTime(Date expiryTime) { ExpiryTime = expiryTime; }

    public Integer getTrangThai() { return TrangThai; }
    public void setTrangThai(Integer trangThai) { TrangThai = trangThai; }

    public Date getNgayTao() { return NgayTao; }
    public void setNgayTao(Date ngayTao) { NgayTao = ngayTao; }
}
