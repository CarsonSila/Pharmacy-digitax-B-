package com.example.HeThongBanThuoc.model;

import java.time.LocalDateTime;

/*
 * Lớp TinNhan mô tả một tin nhắn trong phiên tư vấn giữa khách hàng và AI.
 * Mỗi tin nhắn có thông tin người gửi, nội dung, và thời gian.
 */
public class TinNhan {
    private Integer maTinNhan;
    private Integer maTuVan;
    private String nguoiDung;
    private String noiDung;
    private LocalDateTime thoiGian;
    /*
     *Constructor mặc định (KHÔNG tham số)
     * DÙNG CHO:
     * - Jackson (khi Spring Boot parse JSON)*/

    public TinNhan() {}

    /*
     * Constructor dành cho tin nhắn mới (3 tham số)
     * DÙNG CHO:
     * - Khi tạo tin nhắn ở backend (không cần truyền thời gian)
     * - Thời gian sẽ tự động là LocalDateTime.now() */

    public TinNhan(Integer maTuVan, String nguoiDung, String noiDung) {
        this.maTuVan = maTuVan;
        this.nguoiDung = nguoiDung;
        this.noiDung = noiDung;
        this.thoiGian = LocalDateTime.now(); // Tự động lấy thời gian hiện tại
    }

    /*
     *Constructor rút gọn (dùng boolean isFromUser)
     * DÙNG CHO:
     * - Ứng dụng Android hoặc service chỉ biết "người gửi" là user/AI
     * - Giúp tránh gõ thủ công "Khách hàng" hoặc "AI"*/

    public TinNhan(Integer maTuVan, String noiDung, boolean isFromUser) {
        this.maTuVan = maTuVan;
        this.nguoiDung = isFromUser ? "Khách hàng" : "AI";
        this.noiDung = noiDung;
        this.thoiGian = LocalDateTime.now();
    }

    /*
     *Constructor đầy đủ (4 tham số)
     * DÙNG CHO:
     * - Khi cần tạo tin nhắn có thời gian cụ thể (ví dụ: lấy từ DB hoặc API)
     * - Phù hợp cho phần Controller khi muốn truyền thời gian String*/

    public TinNhan(Integer maTuVan, String nguoiDung, String noiDung, String thoiGian) {
        this.maTuVan = maTuVan;
        this.nguoiDung = nguoiDung;
        this.noiDung = noiDung;
        // Parse String sang LocalDateTime
        this.thoiGian = LocalDateTime.parse(thoiGian);
    }

    // GETTERS & SETTERS

    public Integer getMaTinNhan() {
        return maTinNhan;
    }

    public void setMaTinNhan(Integer maTinNhan) {
        this.maTinNhan = maTinNhan;
    }

    public Integer getMaTuVan() {
        return maTuVan;
    }

    public void setMaTuVan(Integer maTuVan) {
        this.maTuVan = maTuVan;
    }

    public String getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(String nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }
    // Các hàm tiện ích (hỗ trợ hiển thị UI)

    /* Trả về true nếu tin nhắn là từ khách hàng */
    public boolean isFromUser() {
        return "Khách hàng".equalsIgnoreCase(nguoiDung);
    }

    /* Trả về true nếu tin nhắn là từ AI */
    public boolean isFromAI() {
        return "AI".equalsIgnoreCase(nguoiDung);
    }
}
