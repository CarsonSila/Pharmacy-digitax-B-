package com.example.hethongbanthuoc.feature_rewards.models;

import java.io.Serializable;
public class Model_QuaTang implements Serializable {
    private int maQuaTang;
    private String tenQuaTang;
    private String moTa;
    private String hinhAnh;
    private int diemCanDoi;
    private int soLuongTon;
    private boolean trangThai;
    private String ngayTao;
    private String ngayCapNhat;

    // Constructors
    public Model_QuaTang() {}

    public Model_QuaTang(int maQuaTang, String tenQuaTang, String moTa, String hinhAnh,
                         int diemCanDoi, int soLuongTon, boolean trangThai) {
        this.maQuaTang = maQuaTang;
        this.tenQuaTang = tenQuaTang;
        this.moTa = moTa;
        this.hinhAnh = hinhAnh;
        this.diemCanDoi = diemCanDoi;
        this.soLuongTon = soLuongTon;
        this.trangThai = trangThai;
    }

    // Getters and Setters
    public int getMaQuaTang() { return maQuaTang; }
    public void setMaQuaTang(int maQuaTang) { this.maQuaTang = maQuaTang; }

    public String getTenQuaTang() { return tenQuaTang; }
    public void setTenQuaTang(String tenQuaTang) { this.tenQuaTang = tenQuaTang; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public int getDiemCanDoi() { return diemCanDoi; }
    public void setDiemCanDoi(int diemCanDoi) { this.diemCanDoi = diemCanDoi; }

    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public String getNgayTao() { return ngayTao; }
    public void setNgayTao(String ngayTao) { this.ngayTao = ngayTao; }

    public String getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(String ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}

