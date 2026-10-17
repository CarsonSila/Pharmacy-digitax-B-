package com.example.HeThongBanThuoc.model;

import java.time.LocalDateTime;

public class LichSuDoiQua {
    private Integer maDoiQua;
    private Integer maNguoiDung;
    private Integer maQuaTang;
    private Integer diemSuDung;
    private Integer soLuong;
    private String tenQuaTang;
    private String hinhAnh; //
    private Integer diemCanDoiLucDoi;
    private String ghiChu;
    private String trangThai; // Chờ xử lý, Đã duyệt, Đổi thành công, Từ chối
    private String lyDoTuChoi;
    private LocalDateTime ngayDoi;
    private LocalDateTime ngayXuLy;
    private Integer nguoiXuLy;

    // Constructors
    public LichSuDoiQua() {}

    public LichSuDoiQua(Integer maNguoiDung, Integer maQuaTang, Integer diemSuDung,
                        Integer soLuong, String tenQuaTang, Integer diemCanDoiLucDoi) {
        this.maNguoiDung = maNguoiDung;
        this.maQuaTang = maQuaTang;
        this.diemSuDung = diemSuDung;
        this.soLuong = soLuong;
        this.tenQuaTang = tenQuaTang;
        this.diemCanDoiLucDoi = diemCanDoiLucDoi;
        this.trangThai = "Chờ xử lý";
    }

    // Getters and Setters
    public Integer getMaDoiQua() { return maDoiQua; }
    public void setMaDoiQua(Integer maDoiQua) { this.maDoiQua = maDoiQua; }

    public Integer getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(Integer maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public Integer getMaQuaTang() { return maQuaTang; }
    public void setMaQuaTang(Integer maQuaTang) { this.maQuaTang = maQuaTang; }

    public Integer getDiemSuDung() { return diemSuDung; }
    public void setDiemSuDung(Integer diemSuDung) { this.diemSuDung = diemSuDung; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public String getTenQuaTang() { return tenQuaTang; }
    public void setTenQuaTang(String tenQuaTang) { this.tenQuaTang = tenQuaTang; }

    public String getHinhAnh() { return hinhAnh; } // ← THÊM GETTER
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; } // ← THÊM SETTER

    public Integer getDiemCanDoiLucDoi() { return diemCanDoiLucDoi; }
    public void setDiemCanDoiLucDoi(Integer diemCanDoiLucDoi) { this.diemCanDoiLucDoi = diemCanDoiLucDoi; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getLyDoTuChoi() { return lyDoTuChoi; }
    public void setLyDoTuChoi(String lyDoTuChoi) { this.lyDoTuChoi = lyDoTuChoi; }

    public LocalDateTime getNgayDoi() { return ngayDoi; }
    public void setNgayDoi(LocalDateTime ngayDoi) { this.ngayDoi = ngayDoi; }

    public LocalDateTime getNgayXuLy() { return ngayXuLy; }
    public void setNgayXuLy(LocalDateTime ngayXuLy) { this.ngayXuLy = ngayXuLy; }

    public Integer getNguoiXuLy() { return nguoiXuLy; }
    public void setNguoiXuLy(Integer nguoiXuLy) { this.nguoiXuLy = nguoiXuLy; }
}