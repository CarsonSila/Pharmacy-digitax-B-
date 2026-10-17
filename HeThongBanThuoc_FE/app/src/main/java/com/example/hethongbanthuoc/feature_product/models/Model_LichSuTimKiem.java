package com.example.hethongbanthuoc.feature_product.models;

import com.google.gson.annotations.SerializedName;

public class Model_LichSuTimKiem {

    @SerializedName("maLichSu")
    private Integer maLichSu;

    @SerializedName("maNguoiDung")
    private Integer maNguoiDung;

    @SerializedName("tuKhoa")
    private String tuKhoa;

    @SerializedName("thoiGianTimKiem")
    private String thoiGianTimKiem;

    // Constructor
    public Model_LichSuTimKiem() {}

    public Model_LichSuTimKiem(Integer maLichSu, String tuKhoa, String thoiGianTimKiem) {
        this.maLichSu = maLichSu;
        this.tuKhoa = tuKhoa;
        this.thoiGianTimKiem = thoiGianTimKiem;
    }

    // Getters & Setters
    public Integer getMaLichSu() {
        return maLichSu;
    }

    public void setMaLichSu(Integer maLichSu) {
        this.maLichSu = maLichSu;
    }

    public Integer getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(Integer maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getTuKhoa() {
        return tuKhoa;
    }

    public void setTuKhoa(String tuKhoa) {
        this.tuKhoa = tuKhoa;
    }

    public String getThoiGianTimKiem() {
        return thoiGianTimKiem;
    }

    public void setThoiGianTimKiem(String thoiGianTimKiem) {
        this.thoiGianTimKiem = thoiGianTimKiem;
    }
}