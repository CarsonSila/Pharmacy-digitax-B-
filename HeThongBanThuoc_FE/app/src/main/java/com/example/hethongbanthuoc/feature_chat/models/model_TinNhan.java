package com.example.hethongbanthuoc.feature_chat.models;

public class model_TinNhan {
    private Integer maPhienTuVan;
    private String nguoiGui;    // "Khách hàng" hoặc "AI"
    private String noiDung;
    private String thoiGian;    // dạng "HH:mm" hoặc ISO string
    private boolean fromUser;

    public model_TinNhan() {}

    // đầy đủ
    public model_TinNhan(Integer maPhienTuVan, String nguoiGui, String noiDung, String thoiGian) {
        this.maPhienTuVan = maPhienTuVan;
        this.nguoiGui = nguoiGui;
        this.noiDung = noiDung;
        this.thoiGian = thoiGian;
        this.fromUser = "Khách hàng".equalsIgnoreCase(nguoiGui);
    }

    // tiện lợi: (maPhienTuVan, noiDung, isFromUser)
    public model_TinNhan(Integer maPhienTuVan, String noiDung, boolean isFromUser) {
        this.maPhienTuVan = maPhienTuVan;
        this.fromUser = isFromUser;
        this.nguoiGui = isFromUser ? "Khách hàng" : "AI";
        this.noiDung = noiDung;
        this.thoiGian = null;
    }

    // tiện lợi với thời gian string
    public model_TinNhan(Integer maPhienTuVan, String noiDung, boolean isFromUser, String thoiGian) {
        this.maPhienTuVan = maPhienTuVan;
        this.fromUser = isFromUser;
        this.nguoiGui = isFromUser ? "Khách hàng" : "AI";
        this.noiDung = noiDung;
        this.thoiGian = thoiGian;
    }

    // getters / setters
    public Integer getMaPhienTuVan() { return maPhienTuVan; }
    public void setMaPhienTuVan(Integer maPhienTuVan) { this.maPhienTuVan = maPhienTuVan; }

    public String getNguoiGui() { return nguoiGui; }
    public void setNguoiGui(String nguoiGui) { this.nguoiGui = nguoiGui; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getThoiGian() { return thoiGian; }
    public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }

    public boolean isFromUser() { return fromUser; }
    public void setFromUser(boolean fromUser) { this.fromUser = fromUser; }
}
