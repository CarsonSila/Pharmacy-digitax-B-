package com.example.hethongbanthuoc.feature_shopping.cart.models;

public class Model_ChiTietGioHangRequest {
    private int maGioHang;
    private int maSanPham;
    private int soLuong;

    public Model_ChiTietGioHangRequest(int maGioHang, int maSanPham, int soLuong) {
        this.maGioHang = maGioHang;
        this.maSanPham = maSanPham;
        this.soLuong = soLuong;
    }

    public int getMaGioHang() {
        return maGioHang;
    }

    public void setMaGioHang(int maGioHang) {
        this.maGioHang = maGioHang;
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
}