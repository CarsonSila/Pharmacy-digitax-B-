package com.example.hethongbanthuoc.feature_chat.models;

import com.google.gson.annotations.SerializedName;

public class model_ChatMessage {
    @SerializedName("noiDung")
    private String noiDung;

    @SerializedName("nguoiDung")
    private String nguoiDung;

    @SerializedName("thoiGian")
    private String thoiGian;

    public model_ChatMessage() {}

    public model_ChatMessage(String noiDung, String nguoiDung, String thoiGian) {
        this.noiDung = noiDung;
        this.nguoiDung = nguoiDung;
        this.thoiGian = thoiGian;
    }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getNguoiDung() { return nguoiDung; }
    public void setNguoiDung(String nguoiDung) { this.nguoiDung = nguoiDung; }

    public String getThoiGian() { return thoiGian; }
    public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }

    // Chuyển sang TinNhan để hiển thị
    public model_TinNhan toTinNhan(Integer maTuVan) {
        return new model_TinNhan(maTuVan, nguoiDung, noiDung, thoiGian);
    }
}
