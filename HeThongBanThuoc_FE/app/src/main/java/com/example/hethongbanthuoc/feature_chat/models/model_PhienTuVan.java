package com.example.hethongbanthuoc.feature_chat.models;

public class model_PhienTuVan {
    private Integer maPhienTuVan;
    private String trangThai; // "Đang mở", "Đang xử lý", "Đã kết thúc"
    private String ngayTao;
    private String ngayCapNhat;
    private Integer soTinNhan;
    private String tinNhanCuoi;

    public model_PhienTuVan() {}

    public Integer getMaPhienTuVan() {
        return maPhienTuVan;
    }

    public void setMaPhienTuVan(Integer maPhienTuVan) {
        this.maPhienTuVan = maPhienTuVan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(String ngayTao) {
        this.ngayTao = ngayTao;
    }

    public String getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(String ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public Integer getSoTinNhan() {
        return soTinNhan;
    }

    public void setSoTinNhan(Integer soTinNhan) {
        this.soTinNhan = soTinNhan;
    }

    public String getTinNhanCuoi() {
        return tinNhanCuoi;
    }

    public void setTinNhanCuoi(String tinNhanCuoi) {
        this.tinNhanCuoi = tinNhanCuoi;
    }

    public boolean isDangMo() {
        return "Đang mở".equals(trangThai);
    }

    public boolean isDaKetThuc() {
        return "Đã kết thúc".equals(trangThai);
    }

    /*
     * Setter tiện lợi để đồng bộ trạng thái
     * @param dangMo true → "Đang mở", false → "Đã kết thúc"
     */
    public void setDangMo(boolean dangMo) {
        this.trangThai = dangMo ? "Đang mở" : "Đã kết thúc";
    }
}
