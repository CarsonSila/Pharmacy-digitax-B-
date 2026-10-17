package com.example.HeThongBanThuoc.model;

import java.io.Serializable;

/*
 * CustomUserPrincipal lưu thông tin user trong SecurityContext
 * Dùng để xác thực mọi nghiệp vụ của user
 */
public class CustomUserPrincipal implements Serializable {
    private final Integer userId; // ID người dùng trong DB
    private final String email;   // Email người dùng

    public CustomUserPrincipal(Integer userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public Integer getUserId() {
        return userId;
        // Luôn dùng để thao tác dữ liệu thuộc về user hiện tại
        // VD: Lấy danh sách địa chỉ, đặt địa chỉ mặc định, xem đơn hàng
    }

    public String getEmail() {
        return email;
        // Chỉ dùng để hiển thị hoặc tạo JWT
    }

    @Override
    public String toString() {
        return "CustomUserPrincipal{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                '}';
    }

    /*
     *  Nghiệp vụ ngắn gọn
     * 1. Khi đăng nhập thành công: backend tạo đối tượng này dựa trên userId + email
     * 2. Encode vào JWT trả về FE
     * 3. Mọi request bảo mật:
     *      - JWT được gửi kèm header Authorization
     *      - JWT Filter giải mã → tạo CustomUserPrincipal → gắn vào SecurityContext
     * 4. Controller / Service gọi getUserId() để thao tác dữ liệu đúng user
     * 5. Đảm bảo: user chỉ thao tác dữ liệu của chính mình
     */
}
