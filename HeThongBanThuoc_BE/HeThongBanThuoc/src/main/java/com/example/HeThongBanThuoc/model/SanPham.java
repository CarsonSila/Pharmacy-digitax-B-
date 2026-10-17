package com.example.HeThongBanThuoc.model;

public class SanPham {
    private Integer maSanPham;
    private String maThuoc;
    private String tenThuoc;
    private String moTaNgan;
    private Double gia;
    private String nhaSanXuat;
    private String duongDanAnh;
    private Integer tongBan; // số lượng đã bán (dùng cho API bán chạy)
    private String tenDanhMuc;
    private String tenDonVi;            // Đơn vị tính (Hộp, Chai, Viên...)
    private String moTaDonVi;           // Mô tả đơn vị (VD: "Hộp 20 Gói x 5g")
    private Integer soLuongTonKho;      // Số lượng còn trong kho
    private Integer trangThaiBan;       // 0 = ngừng bán, 1 = đang bán

    public Integer getMaSanPham() { return maSanPham; }
    public void setMaSanPham(Integer maSanPham) { this.maSanPham = maSanPham; }

    public String getMaThuoc() { return maThuoc; }
    public void setMaThuoc(String maThuoc) { this.maThuoc = maThuoc; }

    public String getTenThuoc() { return tenThuoc; }
    public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }

    public String getMoTaNgan() { return moTaNgan; }
    public void setMoTaNgan(String moTaNgan) { this.moTaNgan = moTaNgan; }

    public Double getGia() { return gia; }
    public void setGia(Double gia) { this.gia = gia; }

    public String getNhaSanXuat() { return nhaSanXuat; }
    public void setNhaSanXuat(String nhaSanXuat) { this.nhaSanXuat = nhaSanXuat; }

    public String getDuongDanAnh() { return duongDanAnh; }
    public void setDuongDanAnh(String duongDanAnh) { this.duongDanAnh = duongDanAnh; }

    public Integer getTongBan() { return tongBan; }
    public void setTongBan(Integer tongBan) { this.tongBan = tongBan; }

    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }

    public String getTenDonVi() { return tenDonVi; }
    public void setTenDonVi(String tenDonVi) { this.tenDonVi = tenDonVi; }

    public String getMoTaDonVi() { return moTaDonVi; }
    public void setMoTaDonVi(String moTaDonVi) { this.moTaDonVi = moTaDonVi; }

    public Integer getSoLuongTonKho() { return soLuongTonKho; }
    public void setSoLuongTonKho(Integer soLuongTonKho) { this.soLuongTonKho = soLuongTonKho; }

    public Integer getTrangThaiBan() { return trangThaiBan; }
    public void setTrangThaiBan(Integer trangThaiBan) { this.trangThaiBan = trangThaiBan; }
}