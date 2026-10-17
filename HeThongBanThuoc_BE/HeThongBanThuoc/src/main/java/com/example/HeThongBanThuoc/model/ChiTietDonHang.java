package com.example.HeThongBanThuoc.model;

import java.math.BigDecimal;

public class ChiTietDonHang {
    private Integer maChiTiet;
    private Integer maDonHang;
    private Integer maSanPham;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;

    // Thông tin sản phẩm (join từ bảng san_pham)
    private String tenThuoc;
    private String hinhAnh;

    // Getters & Setters
    public Integer getMaChiTiet() {
        return maChiTiet;
    }

    public void setMaChiTiet(Integer maChiTiet) {
        this.maChiTiet = maChiTiet;
    }

    public Integer getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(Integer maDonHang) {
        this.maDonHang = maDonHang;
    }

    public Integer getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(Integer maSanPham) {
        this.maSanPham = maSanPham;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    public String getTenThuoc() {
        return tenThuoc;
    }

    public void setTenThuoc(String tenSanPham) {
        this.tenThuoc = tenSanPham;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }
}