package com.example.HeThongBanThuoc.model;

import java.time.LocalDateTime;
public class QuaTang {
    private Integer maQuaTang;
    private String tenQuaTang;
    private String moTa;
    private String hinhAnh;
    private Integer diemCanDoi;
    private Integer soLuongTon;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    // Constructors
    public QuaTang() {}

    public QuaTang(Integer maQuaTang, String tenQuaTang, String moTa, String hinhAnh,
                   Integer diemCanDoi, Integer soLuongTon, Boolean trangThai) {
        this.maQuaTang = maQuaTang;
        this.tenQuaTang = tenQuaTang;
        this.moTa = moTa;
        this.hinhAnh = hinhAnh;
        this.diemCanDoi = diemCanDoi;
        this.soLuongTon = soLuongTon;
        this.trangThai = trangThai;
    }

    // Getters and Setters
    public Integer getMaQuaTang() { return maQuaTang; }
    public void setMaQuaTang(Integer maQuaTang) { this.maQuaTang = maQuaTang; }

    public String getTenQuaTang() { return tenQuaTang; }
    public void setTenQuaTang(String tenQuaTang) { this.tenQuaTang = tenQuaTang; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public Integer getDiemCanDoi() { return diemCanDoi; }
    public void setDiemCanDoi(Integer diemCanDoi) { this.diemCanDoi = diemCanDoi; }

    public Integer getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(Integer soLuongTon) { this.soLuongTon = soLuongTon; }

    public Boolean getTrangThai() { return trangThai; }
    public void setTrangThai(Boolean trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public LocalDateTime getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(LocalDateTime ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}
