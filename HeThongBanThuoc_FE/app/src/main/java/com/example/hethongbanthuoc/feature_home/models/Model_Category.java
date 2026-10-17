package com.example.hethongbanthuoc.feature_home.models;

import com.google.gson.annotations.SerializedName;

public class Model_Category {

    @SerializedName("maDanhMuc")
    private Integer maDanhMuc;

    @SerializedName("tenDanhMuc")
    private String tenDanhMuc;

    @SerializedName("slug")
    private String slug;

    @SerializedName("icon")
    private String icon;

    @SerializedName("maDanhMucCha")
    private Integer maDanhMucCha;

    @SerializedName("soLuongSanPham")
    private Integer soLuongSanPham;

    // Getters & Setters
    public Integer getMaDanhMuc() {
        return maDanhMuc;
    }

    public void setMaDanhMuc(Integer maDanhMuc) {
        this.maDanhMuc = maDanhMuc;
    }

    public String getTenDanhMuc() {
        return tenDanhMuc;
    }

    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getMaDanhMucCha() {
        return maDanhMucCha;
    }

    public void setMaDanhMucCha(Integer maDanhMucCha) {
        this.maDanhMucCha = maDanhMucCha;
    }

    public Integer getSoLuongSanPham() {
        return soLuongSanPham;
    }

    public void setSoLuongSanPham(Integer soLuongSanPham) {
        this.soLuongSanPham = soLuongSanPham;
    }
}