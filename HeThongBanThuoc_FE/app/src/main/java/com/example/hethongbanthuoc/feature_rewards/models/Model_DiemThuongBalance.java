package com.example.hethongbanthuoc.feature_rewards.models;

import java.io.Serializable;

// Đóng vai trò là respnse để parce dữ liệu
public class Model_DiemThuongBalance implements Serializable {
    private int maNguoiDung;
    private int tongDiem;
    private int diemSapHetHan;
    private String ngayHetHanGanNhat;

    public Model_DiemThuongBalance() {}

    // Getters and Setters
    public int getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(int maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public int getTongDiem() { return tongDiem; }
    public void setTongDiem(int tongDiem) { this.tongDiem = tongDiem; }

    public int getDiemSapHetHan() { return diemSapHetHan; }
    public void setDiemSapHetHan(int diemSapHetHan) { this.diemSapHetHan = diemSapHetHan; }

    public String getNgayHetHanGanNhat() { return ngayHetHanGanNhat; }
    public void setNgayHetHanGanNhat(String ngayHetHanGanNhat) {
        this.ngayHetHanGanNhat = ngayHetHanGanNhat;
    }
}