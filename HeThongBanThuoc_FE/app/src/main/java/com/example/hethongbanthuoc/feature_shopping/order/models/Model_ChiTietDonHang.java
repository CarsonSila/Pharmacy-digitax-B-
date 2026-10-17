package com.example.hethongbanthuoc.feature_shopping.order.models;

import com.google.gson.annotations.SerializedName;

/*
* CHI TIẾT SẢN PHẨM TRONG ĐƠN ĐÃ TẠO
* Đại diện cho 1 sản phẩm trong đơn hàng đã tồn tại
 */
public class Model_ChiTietDonHang {

    @SerializedName("maChiTiet")
    private int maChiTiet;

    @SerializedName("maDonHang")
    private int maDonHang;

    @SerializedName("maSanPham")
    private int maSanPham;

    @SerializedName("soLuong")
    private int soLuong;

    @SerializedName("donGia")
    private double donGia;

    @SerializedName("thanhTien")
    private double thanhTien;

    @SerializedName("tenThuoc")
    private String tenThuoc;

    @SerializedName("hinhAnh")
    private String hinhAnh;

    // Constructors
    public Model_ChiTietDonHang() {
    }

    public Model_ChiTietDonHang(int maChiTiet, int maDonHang, int maSanPham,
                                int soLuong, double donGia, double thanhTien,
                                String tenSanPham, String hinhAnh) {
        this.maChiTiet = maChiTiet;
        this.maDonHang = maDonHang;
        this.maSanPham = maSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
        this.tenThuoc = tenSanPham;
        this.hinhAnh = hinhAnh;
    }

    // Getters & Setters
    public int getMaChiTiet() {
        return maChiTiet;
    }

    public void setMaChiTiet(int maChiTiet) {
        this.maChiTiet = maChiTiet;
    }

    public int getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(int maDonHang) {
        this.maDonHang = maDonHang;
    }

    public int getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(int maSanPham) {
        this.maSanPham = maSanPham;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }

    public String getTenSanPham() {
        return tenThuoc;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenThuoc = tenSanPham;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }
}