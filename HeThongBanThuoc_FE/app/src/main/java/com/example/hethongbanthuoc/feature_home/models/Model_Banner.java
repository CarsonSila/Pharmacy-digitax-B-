package com.example.hethongbanthuoc.feature_home.models;

public class Model_Banner {
    private Integer maBanner;
    private String hinhAnh;
    private String lienKet;
    private String trangThai;
    private String ngayTao; // dùng String cho dễ parse

    // Getter & Setter
    public Integer getMaBanner() { return maBanner; }
    public void setMaBanner(Integer maBanner) { this.maBanner = maBanner; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public String getLienKet() { return lienKet; }
    public void setLienKet(String lienKet) { this.lienKet = lienKet; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getNgayTao() { return ngayTao; }
    public void setNgayTao(String ngayTao) { this.ngayTao = ngayTao; }
}
