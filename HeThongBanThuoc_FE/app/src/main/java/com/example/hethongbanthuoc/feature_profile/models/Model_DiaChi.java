package com.example.hethongbanthuoc.feature_profile.models;

import java.util.Date;

public class Model_DiaChi {
    private Integer maDiaChi;
    private Integer maNguoiDung;
    private String loaiDiaChi;
    private String tenNguoiNhan;
    private String soDienThoai;
    private String diaChiChiTiet;
    private String tinhThanh;
    private String quanHuyen;
    private String phuongXa;
    private Boolean diaChiMacDinh;
    private Date ngayTao;

    // Getters & Setters
    public Integer getMaDiaChi() { return maDiaChi; }
    public void setMaDiaChi(Integer maDiaChi) { this.maDiaChi = maDiaChi; }

    public Integer getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(Integer maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public String getLoaiDiaChi() { return loaiDiaChi; }
    public void setLoaiDiaChi(String loaiDiaChi) { this.loaiDiaChi = loaiDiaChi; }

    public String getTenNguoiNhan() { return tenNguoiNhan; }
    public void setTenNguoiNhan(String tenNguoiNhan) { this.tenNguoiNhan = tenNguoiNhan; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getDiaChiChiTiet() { return diaChiChiTiet; }
    public void setDiaChiChiTiet(String diaChiChiTiet) { this.diaChiChiTiet = diaChiChiTiet; }

    public String getTinhThanh() { return tinhThanh; }
    public void setTinhThanh(String tinhThanh) { this.tinhThanh = tinhThanh; }

    public String getQuanHuyen() { return quanHuyen; }
    public void setQuanHuyen(String quanHuyen) { this.quanHuyen = quanHuyen; }

    public String getPhuongXa() { return phuongXa; }
    public void setPhuongXa(String phuongXa) { this.phuongXa = phuongXa; }

    public Boolean getDiaChiMacDinh() { return diaChiMacDinh; }
    public void setDiaChiMacDinh(Boolean diaChiMacDinh) { this.diaChiMacDinh = diaChiMacDinh; }

    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }
}
