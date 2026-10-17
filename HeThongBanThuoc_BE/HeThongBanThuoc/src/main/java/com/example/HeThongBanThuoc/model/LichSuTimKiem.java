package com.example.HeThongBanThuoc.model;

import java.time.LocalDateTime;

public class LichSuTimKiem {
    private Integer maLichSu;
    private Integer maNguoiDung;
    private String tuKhoa;
    private LocalDateTime thoiGianTimKiem;

    // Getters & Setters
    public Integer getMaLichSu() { return maLichSu; }
    public void setMaLichSu(Integer maLichSu) { this.maLichSu = maLichSu; }

    public Integer getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(Integer maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public String getTuKhoa() { return tuKhoa; }
    public void setTuKhoa(String tuKhoa) { this.tuKhoa = tuKhoa; }

    public LocalDateTime getThoiGianTimKiem() { return thoiGianTimKiem; }
    public void setThoiGianTimKiem(LocalDateTime thoiGianTimKiem) { this.thoiGianTimKiem = thoiGianTimKiem; }
}