package com.example.HeThongBanThuoc.model;

import java.time.LocalDateTime;

public class ThongBao {
    private Integer maThongBao;
    private Integer maNguoiDung;
    private String tieuDe;
    private String noiDung;
    private String loaiThongBao; // 'Đơn hàng','Hệ thống','Khuyến mãi','Đổi quà','Tư vấn'
    private String trangThai; // 'Chưa đọc','Đã đọc'
    private LocalDateTime ngayTao;
    private LocalDateTime ngayDoc;

    // Constructor
    public ThongBao() {}

    public ThongBao(Integer maNguoiDung, String tieuDe, String noiDung, String loaiThongBao) {
        this.maNguoiDung = maNguoiDung;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.loaiThongBao = loaiThongBao;
        this.trangThai = "Chưa đọc";
        this.ngayTao = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getMaThongBao() {
        return maThongBao;
    }

    public void setMaThongBao(Integer maThongBao) {
        this.maThongBao = maThongBao;
    }

    public Integer getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(Integer maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getLoaiThongBao() {
        return loaiThongBao;
    }

    public void setLoaiThongBao(String loaiThongBao) {
        this.loaiThongBao = loaiThongBao;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayDoc() {
        return ngayDoc;
    }

    public void setNgayDoc(LocalDateTime ngayDoc) {
        this.ngayDoc = ngayDoc;
    }

    @Override
    public String toString() {
        return "ThongBao{" +
                "maThongBao=" + maThongBao +
                ", maNguoiDung=" + maNguoiDung +
                ", tieuDe='" + tieuDe + '\'' +
                ", loaiThongBao='" + loaiThongBao + '\'' +
                ", trangThai='" + trangThai + '\'' +
                ", ngayTao=" + ngayTao +
                '}';
    }
}