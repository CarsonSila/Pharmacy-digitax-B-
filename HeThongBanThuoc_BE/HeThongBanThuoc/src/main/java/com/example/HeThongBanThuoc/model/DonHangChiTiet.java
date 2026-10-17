package com.example.HeThongBanThuoc.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/*
 * DTO duy nhất cho cả API list và detail
 * Dùng cho: danh sách đơn hàng + chi tiết đơn hàng
 */
public class DonHangChiTiet {
    // Thông tin đơn hàng cơ bản (dùng cho cả list và detail)
    private Integer maDonHang;
    private Date ngayTao;
    private String trangThai;
    private BigDecimal tongTien;
    private BigDecimal giamGia;
    private BigDecimal tienPhaiTra;

    // Thông tin người nhận (chỉ có khi call API detail)
    private String tenNguoiNhan;
    private String soDienThoai;
    private String diaChiDayDu;

    // Danh sách sản phẩm (có cả list và detail)
    private List<ChiTietDonHang> danhSachSanPham;

    // Constructors
    public DonHangChiTiet() {
    }

    // Constructor cho API list (không có thông tin người nhận)
    public DonHangChiTiet(Integer maDonHang, Date ngayTao, String trangThai,
                          BigDecimal tongTien, BigDecimal giamGia, BigDecimal tienPhaiTra) {
        this.maDonHang = maDonHang;
        this.ngayTao = ngayTao;
        this.trangThai = trangThai;
        this.tongTien = tongTien;
        this.giamGia = giamGia;
        this.tienPhaiTra = tienPhaiTra;
    }

    // Getters & Setters
    public Integer getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(Integer maDonHang) {
        this.maDonHang = maDonHang;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public BigDecimal getTongTien() {
        return tongTien;
    }

    public void setTongTien(BigDecimal tongTien) {
        this.tongTien = tongTien;
    }

    public BigDecimal getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(BigDecimal giamGia) {
        this.giamGia = giamGia;
    }

    public BigDecimal getTienPhaiTra() {
        return tienPhaiTra;
    }

    public void setTienPhaiTra(BigDecimal tienPhaiTra) {
        this.tienPhaiTra = tienPhaiTra;
    }

    public String getTenNguoiNhan() {
        return tenNguoiNhan;
    }

    public void setTenNguoiNhan(String tenNguoiNhan) {
        this.tenNguoiNhan = tenNguoiNhan;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getDiaChiDayDu() {
        return diaChiDayDu;
    }

    public void setDiaChiDayDu(String diaChiDayDu) {
        this.diaChiDayDu = diaChiDayDu;
    }

    public List<ChiTietDonHang> getDanhSachSanPham() {
        return danhSachSanPham;
    }

    public void setDanhSachSanPham(List<ChiTietDonHang> danhSachSanPham) {
        this.danhSachSanPham = danhSachSanPham;
    }
}