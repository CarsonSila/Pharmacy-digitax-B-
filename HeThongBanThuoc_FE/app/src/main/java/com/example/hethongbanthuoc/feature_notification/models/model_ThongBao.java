package com.example.hethongbanthuoc.feature_notification.models;

import org.json.JSONObject;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class model_ThongBao implements Serializable {

    // 1. Fields (Thuộc tính)
    private Integer maThongBao;   // Mã thông báo
    private Integer maNguoiDung;  // Mã người dùng nhận thông báo
    private String tieuDe;        // Tiêu đề thông báo
    private String noiDung;       // Nội dung thông báo
    private String loaiThongBao;  // Loại: "Đơn hàng", "Hệ thống", "Khuyến mãi", "Đổi quà", "Tư vấn"
    private String trangThai;     // Trạng thái: "Chưa đọc", "Đã đọc"
    private String ngayTao;       // Ngày tạo (String)
    private String ngayDoc;       // Ngày đọc (String)

    // 2. Constructor
    public model_ThongBao() {}  // Constructor mặc định

    public model_ThongBao(Integer maThongBao, Integer maNguoiDung, String tieuDe, String noiDung,
                          String loaiThongBao, String trangThai, String ngayTao, String ngayDoc) {
        this.maThongBao = maThongBao;
        this.maNguoiDung = maNguoiDung;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.loaiThongBao = loaiThongBao;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.ngayDoc = ngayDoc;
    }

    // 3. Getters & Setters (Truy xuất dữ liệu)
    public Integer getMaThongBao() { return maThongBao; }
    public void setMaThongBao(Integer maThongBao) { this.maThongBao = maThongBao; }

    public Integer getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(Integer maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getLoaiThongBao() { return loaiThongBao; }
    public void setLoaiThongBao(String loaiThongBao) { this.loaiThongBao = loaiThongBao; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getNgayTao() { return ngayTao; }
    public void setNgayTao(String ngayTao) { this.ngayTao = ngayTao; }

    public String getNgayDoc() { return ngayDoc; }
    public void setNgayDoc(String ngayDoc) { this.ngayDoc = ngayDoc; }

    //  4. Helper Methods

    // 4.1 Kiểm tra thông báo chưa đọc
    public boolean isChuaDoc() {
        return "Chưa đọc".equals(trangThai);
    }

    // 4.2 Format ngày tạo: từ "yyyy-MM-ddTHH:mm:ss" -> "dd/MM/yyyy HH:mm"
    public String getFormattedDate() {
        if (ngayTao == null || ngayTao.isEmpty()) return "";
        try {
            String[] parts = ngayTao.split("T");
            if (parts.length == 2) {
                String[] dateParts = parts[0].split("-");
                String[] timeParts = parts[1].split(":");
                if (dateParts.length == 3 && timeParts.length >= 2) {
                    return dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0] +
                            " " + timeParts[0] + ":" + timeParts[1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ngayTao;
    }

    // 4.3 Tính thời gian đã trôi qua kể từ khi tạo thông báo
    public String getTimeAgo() {
        if (ngayTao == null || ngayTao.isEmpty()) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = sdf.parse(ngayTao);
            long diff = System.currentTimeMillis() - date.getTime();

            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) return days + " ngày trước";
            else if (hours > 0) return hours + " giờ trước";
            else if (minutes > 0) return minutes + " phút trước";
            else return "Vừa xong";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // 4.4 Lấy icon theo loại thông báo
    public int getIconResource() {
        switch (loaiThongBao) {
            case "Đơn hàng": return android.R.drawable.ic_menu_sort_by_size;
            case "Khuyến mãi": return android.R.drawable.ic_menu_today;
            case "Đổi quà": return android.R.drawable.ic_menu_gallery;
            case "Tư vấn": return android.R.drawable.ic_menu_help;
            case "Hệ thống":
            default: return android.R.drawable.ic_dialog_info;
        }
    }

    // 4.5 Lấy màu theo loại thông báo
    public int getColorResource() {
        switch (loaiThongBao) {
            case "Đơn hàng": return android.R.color.holo_blue_light;
            case "Khuyến mãi": return android.R.color.holo_red_light;
            case "Đổi quà": return android.R.color.holo_green_light;
            case "Tư vấn": return android.R.color.holo_orange_light;
            case "Hệ thống":
            default: return android.R.color.darker_gray;
        }
    }

    //  5. ToString (debug/log)
    @Override
    public String toString() {
        return "ThongBao{" +
                "maThongBao=" + maThongBao +
                ", tieuDe='" + tieuDe + '\'' +
                ", loaiThongBao='" + loaiThongBao + '\'' +
                ", trangThai='" + trangThai + '\'' +
                ", ngayTao='" + ngayTao + '\'' +
                '}';
    }

    // 6. JSON Parsing (Thủ công)
    // Chuyển từ JSONObject sang object ThongBao
    public static model_ThongBao fromJson(JSONObject json) {
        model_ThongBao tb = new model_ThongBao();
        tb.maThongBao = json.optInt("maThongBao");
        tb.maNguoiDung = json.optInt("maNguoiDung");
        tb.tieuDe = json.optString("tieuDe");
        tb.noiDung = json.optString("noiDung");
        tb.loaiThongBao = json.optString("loaiThongBao");
        tb.trangThai = json.optString("trangThai");
        tb.ngayTao = json.optString("ngayTao");
        tb.ngayDoc = json.optString("ngayDoc");
        return tb;
    }
}
