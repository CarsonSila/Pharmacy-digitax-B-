package com.example.HeThongBanThuoc.model;

import java.time.LocalDateTime;

public class GioHang {
    private Integer maGioHang;
    private Integer maNguoiDung;
    private String tenNguoiDung;
    private String trangThai;
    private LocalDateTime ngayTao;

    // 🔹 Thuộc tính mới — tổng tiền giỏ hàng

    // ✅ Constructor mặc định
    public GioHang() {}

    // ✅ Constructor thêm mới
    public GioHang(int maNguoiDung, String trangThai) {
        this.maNguoiDung = maNguoiDung;
        this.trangThai = trangThai;
    }

    // =========================
    // 🔹 Getter - Setter
    // =========================
    public Integer getMaGioHang() {
        return maGioHang;
    }

    public void setMaGioHang(Integer maGioHang) {
        this.maGioHang = maGioHang;
    }

    public Integer getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(Integer maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getTenNguoiDung() {
        return tenNguoiDung;
    }

    public void setTenNguoiDung(String tenNguoiDung) {
        this.tenNguoiDung = tenNguoiDung;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}
