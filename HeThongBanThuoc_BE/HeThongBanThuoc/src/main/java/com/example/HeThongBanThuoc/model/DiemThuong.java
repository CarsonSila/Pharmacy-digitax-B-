package com.example.HeThongBanThuoc.model;

import java.time.LocalDateTime;

public class DiemThuong {
    private Integer maDiem;
    private Integer maNguoiDung;
    private Integer maDonHang;
    private String loaiGiaoDich; // Tich, Tieu, HetHan, HoanLai
    private Integer soDiem;
    private Integer soDiemConLai;
    private String moTa;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayHetHan;
    private String trangThai; // HieuLuc, DaSuDung, HetHan

    // Constructors
    public DiemThuong() {}

    public DiemThuong(Integer maNguoiDung, Integer maDonHang, String loaiGiaoDich,
                      Integer soDiem, Integer soDiemConLai, String moTa, LocalDateTime ngayHetHan) {
        this.maNguoiDung = maNguoiDung;
        this.maDonHang = maDonHang;
        this.loaiGiaoDich = loaiGiaoDich;
        this.soDiem = soDiem;
        this.soDiemConLai = soDiemConLai;
        this.moTa = moTa;
        this.ngayHetHan = ngayHetHan;
        this.trangThai = "HieuLuc";
    }

    // Getters and Setters
    public Integer getMaDiem() { return maDiem; }
    public void setMaDiem(Integer maDiem) { this.maDiem = maDiem; }

    public Integer getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(Integer maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public Integer getMaDonHang() { return maDonHang; }
    public void setMaDonHang(Integer maDonHang) { this.maDonHang = maDonHang; }

    public String getLoaiGiaoDich() { return loaiGiaoDich; }
    public void setLoaiGiaoDich(String loaiGiaoDich) { this.loaiGiaoDich = loaiGiaoDich; }

    public Integer getSoDiem() { return soDiem; }
    public void setSoDiem(Integer soDiem) { this.soDiem = soDiem; }

    public Integer getSoDiemConLai() { return soDiemConLai; }
    public void setSoDiemConLai(Integer soDiemConLai) { this.soDiemConLai = soDiemConLai; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public LocalDateTime getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(LocalDateTime ngayHetHan) { this.ngayHetHan = ngayHetHan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
