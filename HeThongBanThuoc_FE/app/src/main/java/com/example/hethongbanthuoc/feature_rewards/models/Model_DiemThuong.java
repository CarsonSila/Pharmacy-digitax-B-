package com.example.hethongbanthuoc.feature_rewards.models;

import java.io.Serializable;

public class Model_DiemThuong implements Serializable {
    private int maDiem;
    private int maNguoiDung;
    private Integer maDonHang;
    private String loaiGiaoDich; // Tich, Tieu, HetHan, HoanLai
    private int soDiem;
    private int soDiemConLai;
    private String moTa;
    private String ngayTao;
    private String ngayHetHan;
    private String trangThai; // HieuLuc, DaSuDung, HetHan

    public Model_DiemThuong() {}

    // Getters and Setters
    public int getMaDiem() { return maDiem; }
    public void setMaDiem(int maDiem) { this.maDiem = maDiem; }

    public int getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(int maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public Integer getMaDonHang() { return maDonHang; }
    public void setMaDonHang(Integer maDonHang) { this.maDonHang = maDonHang; }

    public String getLoaiGiaoDich() { return loaiGiaoDich; }
    public void setLoaiGiaoDich(String loaiGiaoDich) { this.loaiGiaoDich = loaiGiaoDich; }

    public int getSoDiem() { return soDiem; }
    public void setSoDiem(int soDiem) { this.soDiem = soDiem; }

    public int getSoDiemConLai() { return soDiemConLai; }
    public void setSoDiemConLai(int soDiemConLai) { this.soDiemConLai = soDiemConLai; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getNgayTao() { return ngayTao; }
    public void setNgayTao(String ngayTao) { this.ngayTao = ngayTao; }

    public String getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(String ngayHetHan) { this.ngayHetHan = ngayHetHan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
