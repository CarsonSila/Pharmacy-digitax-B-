package com.example.HeThongBanThuoc.model;

import java.time.LocalDateTime;

public class TuKhoaTimKiem {
    private Integer maTuKhoa;
    private String tuKhoa;
    private Integer soLanTimKiem;
    private LocalDateTime lanTimKiemCuoi;
    private LocalDateTime ngayTao;

    // Getters & Setters
    public Integer getMaTuKhoa() { return maTuKhoa; }
    public void setMaTuKhoa(Integer maTuKhoa) { this.maTuKhoa = maTuKhoa; }

    public String getTuKhoa() { return tuKhoa; }
    public void setTuKhoa(String tuKhoa) { this.tuKhoa = tuKhoa; }

    public Integer getSoLanTimKiem() { return soLanTimKiem; }
    public void setSoLanTimKiem(Integer soLanTimKiem) { this.soLanTimKiem = soLanTimKiem; }

    public LocalDateTime getLanTimKiemCuoi() { return lanTimKiemCuoi; }
    public void setLanTimKiemCuoi(LocalDateTime lanTimKiemCuoi) { this.lanTimKiemCuoi = lanTimKiemCuoi; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}