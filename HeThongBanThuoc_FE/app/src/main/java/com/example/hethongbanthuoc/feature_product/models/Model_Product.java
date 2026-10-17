package com.example.hethongbanthuoc.feature_product.models;

import com.google.gson.annotations.SerializedName;

public class Model_Product {

    @SerializedName("maSanPham")
    private Integer maSanPham;

    @SerializedName("maThuoc")
    private String maThuoc;

    @SerializedName("tenThuoc")
    private String tenThuoc;

    @SerializedName("moTaNgan")
    private String moTaNgan;

    @SerializedName("gia")
    private Double gia;

    @SerializedName("nhaSanXuat")
    private String nhaSanXuat;

    @SerializedName("duongDanAnh")
    private String duongDanAnh;

    @SerializedName("tongBan")
    private Integer tongBan;

    @SerializedName("tenDanhMuc")
    private String tenDanhMuc;

    @SerializedName("tenDonVi")
    private String tenDonVi;

    @SerializedName("moTaDonVi")
    private String moTaDonVi;

    @SerializedName("soLuongTonKho")
    private Integer soLuongTonKho;

    @SerializedName("trangThaiBan")
    private Integer trangThaiBan;

    // Getters & Setters
    public Integer getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(Integer maSanPham) {
        this.maSanPham = maSanPham;
    }

    public String getMaThuoc() {
        return maThuoc;
    }

    public void setMaThuoc(String maThuoc) {
        this.maThuoc = maThuoc;
    }

    public String getTenThuoc() {
        return tenThuoc;
    }

    public void setTenThuoc(String tenThuoc) {
        this.tenThuoc = tenThuoc;
    }

    public String getMoTaNgan() {
        return moTaNgan;
    }

    public void setMoTaNgan(String moTaNgan) {
        this.moTaNgan = moTaNgan;
    }

    public Double getGia() {
        return gia;
    }

    public void setGia(Double gia) {
        this.gia = gia;
    }

    public String getNhaSanXuat() {
        return nhaSanXuat;
    }

    public void setNhaSanXuat(String nhaSanXuat) {
        this.nhaSanXuat = nhaSanXuat;
    }

    public String getDuongDanAnh() {
        return duongDanAnh;
    }

    public void setDuongDanAnh(String duongDanAnh) {
        this.duongDanAnh = duongDanAnh;
    }

    public Integer getTongBan() {
        return tongBan;
    }

    public void setTongBan(Integer tongBan) {
        this.tongBan = tongBan;
    }

    public String getTenDanhMuc() {
        return tenDanhMuc;
    }

    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
    }

    public String getTenDonVi() {
        return tenDonVi;
    }

    public void setTenDonVi(String tenDonVi) {
        this.tenDonVi = tenDonVi;
    }

    public String getMoTaDonVi() {
        return moTaDonVi;
    }

    public void setMoTaDonVi(String moTaDonVi) {
        this.moTaDonVi = moTaDonVi;
    }

    public Integer getSoLuongTonKho() {
        return soLuongTonKho;
    }

    public void setSoLuongTonKho(Integer soLuongTonKho) {
        this.soLuongTonKho = soLuongTonKho;
    }

    public Integer getTrangThaiBan() {
        return trangThaiBan;
    }

    public void setTrangThaiBan(Integer trangThaiBan) {
        this.trangThaiBan = trangThaiBan;
    }
}