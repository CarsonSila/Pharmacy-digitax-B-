package com.example.HeThongBanThuoc.model;

import java.time.LocalDateTime;

public class Banner {
    private Integer maBanner;
    private String hinhAnh;
    private String lienKet;
    private String trangThai;
    private LocalDateTime ngayTao;

    // Getters & Setters
    public Integer getMaBanner() { return maBanner; }
    public void setMaBanner(Integer maBanner) { this.maBanner = maBanner; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public String getLienKet() { return lienKet; }
    public void setLienKet(String lienKet) { this.lienKet = lienKet; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}
