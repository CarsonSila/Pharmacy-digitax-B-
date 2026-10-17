package com.example.hethongbanthuoc.feature_product.models;

import com.google.gson.annotations.SerializedName;

public class Model_TuKhoaTimKiem {

    @SerializedName("maTuKhoa")
    private Integer maTuKhoa;

    @SerializedName("tuKhoa")
    private String tuKhoa;

    @SerializedName("soLanTimKiem")
    private Integer soLanTimKiem;

    @SerializedName("lanTimKiemCuoi")
    private String lanTimKiemCuoi;

    // Constructor
    public Model_TuKhoaTimKiem() {}

    public Model_TuKhoaTimKiem(String tuKhoa, Integer soLanTimKiem) {
        this.tuKhoa = tuKhoa;
        this.soLanTimKiem = soLanTimKiem;
    }

    // Getters & Setters
    public Integer getMaTuKhoa() {
        return maTuKhoa;
    }

    public void setMaTuKhoa(Integer maTuKhoa) {
        this.maTuKhoa = maTuKhoa;
    }

    public String getTuKhoa() {
        return tuKhoa;
    }

    public void setTuKhoa(String tuKhoa) {
        this.tuKhoa = tuKhoa;
    }

    public Integer getSoLanTimKiem() {
        return soLanTimKiem;
    }

    public void setSoLanTimKiem(Integer soLanTimKiem) {
        this.soLanTimKiem = soLanTimKiem;
    }

    public String getLanTimKiemCuoi() {
        return lanTimKiemCuoi;
    }

    public void setLanTimKiemCuoi(String lanTimKiemCuoi) {
        this.lanTimKiemCuoi = lanTimKiemCuoi;
    }
}