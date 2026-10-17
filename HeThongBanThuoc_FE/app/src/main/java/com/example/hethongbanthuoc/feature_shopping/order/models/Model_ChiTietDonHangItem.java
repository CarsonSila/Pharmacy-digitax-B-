package com.example.hethongbanthuoc.feature_shopping.order.models;

import com.google.gson.annotations.SerializedName;

/*
 * Model chi tiết sản phẩm khi tạo đơn hàng
 * Đại diện cho 1 sản phẩm trong đơn hàng đang tạo
 */
public class Model_ChiTietDonHangItem {

    @SerializedName("maSanPham")
    private int maSanPham;

    @SerializedName("soLuong")
    private int soLuong;

    @SerializedName("donGia")
    private double donGia;

    @SerializedName("thanhTien")
    private double thanhTien;

    // Constructors
    public Model_ChiTietDonHangItem() {
    }

    public Model_ChiTietDonHangItem(int maSanPham, int soLuong, double donGia, double thanhTien) {
        this.maSanPham = maSanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    // Getters & Setters
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
}