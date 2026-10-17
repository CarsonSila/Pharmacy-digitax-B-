package com.example.HeThongBanThuoc.model;

import java.time.LocalDateTime;

public class PhienTuVan {
    private Integer maPhienTuVan;
    private Integer maNguoiGui;
    private Integer maNguoiNhan;
    private String trangThai; // 'Đang mở', 'Đang xử lý', 'Đã kết thúc'
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    // Constructors
    public PhienTuVan() {
    }

    public PhienTuVan(Integer maNguoiGui, String trangThai) {
        this.maNguoiGui = maNguoiGui;
        this.trangThai = trangThai;
        this.ngayTao = LocalDateTime.now();
        this.ngayCapNhat = LocalDateTime.now();
    }

    // Getters & Setters
    public Integer getMaPhienTuVan() {
        return maPhienTuVan;
    }

    public void setMaPhienTuVan(Integer maPhienTuVan) {
        this.maPhienTuVan = maPhienTuVan;
    }

    public Integer getMaNguoiGui() {
        return maNguoiGui;
    }

    public void setMaNguoiGui(Integer maNguoiGui) {
        this.maNguoiGui = maNguoiGui;
    }

    public Integer getMaNguoiNhan() {
        return maNguoiNhan;
    }

    public void setMaNguoiNhan(Integer maNguoiNhan) {
        this.maNguoiNhan = maNguoiNhan;
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

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }
}