package com.example.HeThongBanThuoc.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Model entity đơn hàng - map với bảng DonHang
 */
public class DonHang {
    private Integer maDonHang;
    private Integer maNguoiDung;
    private Integer maDiaChi;
    private Integer maGiamGia;
    private BigDecimal tongTien;
    private BigDecimal giamGia;
    private BigDecimal tienPhaiTra;
    private Integer maPhuongThuc;
    private String trangThai;
    private Date ngayTao;
    private Date ngayCapNhat;

    // Fields bổ sung cho xử lý điểm thưởng (không lưu DB, chỉ dùng tạm)
    private boolean suDungDiemThuong;
    private int diemDaDung;

    private List<ChiTietDonHang> chiTietDonHangList;
    // Constructors
    public DonHang() {
    }

    // Getters & Setters
    public Integer getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(Integer maDonHang) {
        this.maDonHang = maDonHang;
    }

    public Integer getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(Integer maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public Integer getMaDiaChi() {
        return maDiaChi;
    }

    public void setMaDiaChi(Integer maDiaChi) {
        this.maDiaChi = maDiaChi;
    }

    public Integer getMaGiamGia() {
        return maGiamGia;
    }

    public void setMaGiamGia(Integer maGiamGia) {
        this.maGiamGia = maGiamGia;
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

    public Integer getMaPhuongThuc() {
        return maPhuongThuc;
    }

    public void setMaPhuongThuc(Integer maPhuongThuc) {
        this.maPhuongThuc = maPhuongThuc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }

    public Date getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(Date ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public boolean isSuDungDiemThuong() {
        return suDungDiemThuong;
    }

    public void setSuDungDiemThuong(boolean suDungDiemThuong) {
        this.suDungDiemThuong = suDungDiemThuong;
    }

    public int getDiemDaDung() {
        return diemDaDung;
    }

    public void setDiemDaDung(int diemDaDung) {
        this.diemDaDung = diemDaDung;
    }

    public List<ChiTietDonHang> getChiTietDonHangList() {
        return chiTietDonHangList;
    }

    public void setChiTietDonHangList(List<ChiTietDonHang> chiTietDonHangList) {
        this.chiTietDonHangList = chiTietDonHangList;
    }
}