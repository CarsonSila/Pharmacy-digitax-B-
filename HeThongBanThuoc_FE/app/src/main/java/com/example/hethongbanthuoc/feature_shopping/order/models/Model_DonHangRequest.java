package com.example.hethongbanthuoc.feature_shopping.order.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/*
 * Model Request để tạo đơn hàng mới
 * Map với class DonHang.java ở Backend
 * Gửi request từ android lên BE để tạo đơn hàng mới
 */
public class Model_DonHangRequest {

    @SerializedName("maNguoiDung")
    private int maNguoiDung;

    @SerializedName("maDiaChi")
    private Integer maDiaChi;

    @SerializedName("maGiamGia")
    private Integer maGiamGia;

    @SerializedName("tongTien")
    private double tongTien;

    @SerializedName("giamGia")
    private double giamGia;

    @SerializedName("tienPhaiTra")
    private double tienPhaiTra;

    @SerializedName("suDungDiemThuong")
    private boolean suDungDiemThuong;

    @SerializedName("diemDaDung")
    private int diemDaDung;

    // ⭐ QUAN TRỌNG: Danh sách chi tiết đơn hàng
    @SerializedName("chiTietDonHangList")
    private List<Model_ChiTietDonHangItem> chiTietDonHangList;

    // Constructors
    public Model_DonHangRequest() {
    }

    // Getters & Setters
    public int getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(int maNguoiDung) {
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

    public List<Model_ChiTietDonHangItem> getChiTietDonHangList() {
        return chiTietDonHangList;
    }

    public void setChiTietDonHangList(List<Model_ChiTietDonHangItem> chiTietDonHangList) {
        this.chiTietDonHangList = chiTietDonHangList;
    }
}