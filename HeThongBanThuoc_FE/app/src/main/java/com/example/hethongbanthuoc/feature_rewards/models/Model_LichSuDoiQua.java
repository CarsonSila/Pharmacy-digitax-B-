package com.example.hethongbanthuoc.feature_rewards.models;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Model_LichSuDoiQua {
    @SerializedName("maDoiQua")
    private Integer maDoiQua;

    @SerializedName("maNguoiDung")
    private Integer maNguoiDung;

    @SerializedName("maQuaTang")
    private Integer maQuaTang;

    @SerializedName("diemSuDung")
    private Integer diemSuDung;

    @SerializedName("soLuong")
    private Integer soLuong;

    @SerializedName("tenQuaTang")
    private String tenQuaTang;

    @SerializedName("hinhAnh")
    private String hinhAnh;

    @SerializedName("diemCanDoiLucDoi")
    private Integer diemCanDoiLucDoi;

    @SerializedName("ghiChu")
    private String ghiChu;

    @SerializedName("trangThai")
    private String trangThai;

    @SerializedName("lyDoTuChoi")
    private String lyDoTuChoi;

    @SerializedName("ngayDoi")
    private String ngayDoi;

    @SerializedName("ngayXuLy")
    private String ngayXuLy;

    @SerializedName("nguoiXuLy")
    private Integer nguoiXuLy;

    // Constructor
    public Model_LichSuDoiQua() {
    }

    // Getters and Setters
    public Integer getMaDoiQua() {
        return maDoiQua;
    }

    public void setMaDoiQua(Integer maDoiQua) {
        this.maDoiQua = maDoiQua;
    }

    public Integer getMaNguoiDung() {
        return maNguoiDung;
    }

    public void setMaNguoiDung(Integer maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public Integer getMaQuaTang() {
        return maQuaTang;
    }

    public void setMaQuaTang(Integer maQuaTang) {
        this.maQuaTang = maQuaTang;
    }

    public Integer getDiemSuDung() {
        return diemSuDung;
    }

    public void setDiemSuDung(Integer diemSuDung) {
        this.diemSuDung = diemSuDung;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public String getTenQuaTang() {
        return tenQuaTang;
    }

    public void setTenQuaTang(String tenQuaTang) {
        this.tenQuaTang = tenQuaTang;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public Integer getDiemCanDoiLucDoi() {
        return diemCanDoiLucDoi;
    }

    public void setDiemCanDoiLucDoi(Integer diemCanDoiLucDoi) {
        this.diemCanDoiLucDoi = diemCanDoiLucDoi;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getLyDoTuChoi() {
        return lyDoTuChoi;
    }

    public void setLyDoTuChoi(String lyDoTuChoi) {
        this.lyDoTuChoi = lyDoTuChoi;
    }

    public String getNgayDoi() {
        return ngayDoi;
    }

    public void setNgayDoi(String ngayDoi) {
        this.ngayDoi = ngayDoi;
    }

    public String getNgayXuLy() {
        return ngayXuLy;
    }

    public void setNgayXuLy(String ngayXuLy) {
        this.ngayXuLy = ngayXuLy;
    }

    public Integer getNguoiXuLy() {
        return nguoiXuLy;
    }

    public void setNguoiXuLy(Integer nguoiXuLy) {
        this.nguoiXuLy = nguoiXuLy;
    }

    // HELPER METHODS

    /*
     * Lấy text hiển thị của trạng thái
     */
    public String getTrangThaiText() {
        if (trangThai == null) return "Chờ xử lý";
        switch (trangThai.trim()) {
            case "Chờ xử lý":
                return "Chờ xử lý";
            case "Đã duyệt":
            case "Đổi thành công":
                return "Đã duyệt";
            case "Từ chối":
                return "Từ chối";
            default:
                return "Chờ xử lý";
        }
    }

    /*
     * Lấy trạng thái chuẩn hóa cho filter
     */
    public String getTrangThaiNormalized() {
        if (trangThai == null) return "CHO_XU_LY";
        switch (trangThai.trim()) {
            case "Chờ xử lý":
                return "CHO_XU_LY";
            case "Đã duyệt":
            case "Đổi thành công":
                return "DA_DUYET";
            case "Từ chối":
                return "TU_CHOI";
            default:
                return "CHO_XU_LY";
        }
    }

    /*
     * Format ngày giờ từ ISO sang dd/MM/yyyy HH:mm
     */
    public String getNgayDoiFormatted() {
        if (ngayDoi == null || ngayDoi.isEmpty()) return "";

        String[] inputPatterns = {
                "yyyy-MM-dd'T'HH:mm:ss",  // ISO_LOCAL_DATE_TIME
                "yyyy-MM-dd HH:mm:ss"     // fallback
        };

        SimpleDateFormat outputFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        for (String pattern : inputPatterns) {
            try {
                SimpleDateFormat inputFormatter = new SimpleDateFormat(pattern, Locale.getDefault());
                Date date = inputFormatter.parse(ngayDoi);
                if (date != null) {
                    return outputFormatter.format(date);
                }
            } catch (Exception ignored) {
                // Thử pattern khác nếu parse lỗi
            }
        }

        // Nếu tất cả fail thì trả về nguyên bản
        return ngayDoi;
    }
}