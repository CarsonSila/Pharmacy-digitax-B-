package com.example.hethongbanthuoc.feature_shopping.order.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

/*
 * Model chi tiết đơn hàng cho Android
 * Nhận dữ liệu từ BE trả về dơn hàng đã tạo -> dùng cho cả danh sách và chi tiết
 * Map với DonHangChiTiet.java từ Backend
 */
public class Model_ChiTietDonHangRequest {

    // Thông tin đơn hàng cơ bản
    @SerializedName("maDonHang")
    private int maDonHang;

    @SerializedName("ngayTao")
    private Date ngayTao;

    @SerializedName("trangThai")
    private String trangThai;

    @SerializedName("tongTien")
    private double tongTien;

    @SerializedName("giamGia")
    private double giamGia;

    @SerializedName("tienPhaiTra")
    private double tienPhaiTra;

    // Thông tin người nhận (chỉ có khi gọi API detail)
    @SerializedName("tenNguoiNhan")
    private String tenNguoiNhan;

    @SerializedName("soDienThoai")
    private String soDienThoai;

    @SerializedName("diaChiDayDu")
    private String diaChiDayDu;

    // Danh sách sản phẩm
    @SerializedName("danhSachSanPham")
    private List<Model_ChiTietDonHang> danhSachSanPham;

    // Constructors
    public Model_ChiTietDonHangRequest() {
    }

    public Model_ChiTietDonHangRequest(int maDonHang, Date ngayTao, String trangThai,
                                       double tongTien, double giamGia, double tienPhaiTra) {
        this.maDonHang = maDonHang;
        this.ngayTao = ngayTao;
        this.trangThai = trangThai;
        this.tongTien = tongTien;
        this.giamGia = giamGia;
        this.tienPhaiTra = tienPhaiTra;
    }

    // Getters & Setters
    public int getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(int maDonHang) {
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

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public double getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(double giamGia) {
        this.giamGia = giamGia;
    }

    public double getTienPhaiTra() {
        return tienPhaiTra;
    }

    public void setTienPhaiTra(double tienPhaiTra) {
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

    public List<Model_ChiTietDonHang> getDanhSachSanPham() {
        return danhSachSanPham;
    }

    public void setDanhSachSanPham(List<Model_ChiTietDonHang> danhSachSanPham) {
        this.danhSachSanPham = danhSachSanPham;
    }
}