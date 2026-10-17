package com.example.HeThongBanThuoc.controller;

import com.example.HeThongBanThuoc.model.NguoiDung;
import com.example.HeThongBanThuoc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.HeThongBanThuoc.model.CustomUserPrincipal;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    // API: Lấy thông tin profile
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            Object principalObj = SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();

            String email;

            if (principalObj instanceof CustomUserPrincipal) {
                email = ((CustomUserPrincipal) principalObj).getEmail();
            } else if (principalObj instanceof String) {
                // fallback nếu principal là String (username/email)
                email = (String) principalObj;
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Không xác định được người dùng"));
            }

            NguoiDung user = userService.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Người dùng không tồn tại"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("hoTen", user.getHoTen());
            response.put("diemTichLuy", user.getDiemTichLuy());
            response.put("soDienThoai", user.getSoDienThoai());
            response.put("email", user.getEmail());
            response.put("maVaiTro", user.getMaVaiTro());
            response.put("gioiTinh", user.getGioiTinh());
            response.put("anhDaiDien", user.getAnhDaiDien());
            response.put("ngaySinh", user.getNgaySinh());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // API: Cập nhật profile
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> payload) {
        try {
            Object principalObj = SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();

            String email;
            if (principalObj instanceof CustomUserPrincipal) {
                email = ((CustomUserPrincipal) principalObj).getEmail();
            } else if (principalObj instanceof String) {
                email = (String) principalObj;
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Không xác định được người dùng"));
            }

            NguoiDung user = userService.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Người dùng không tồn tại"));
            }

            // Lấy dữ liệu từ payload
            String hoTen = (String) payload.get("hoTen");
            String gioiTinh = (String) payload.get("gioiTinh");
            String soDienThoai = (String) payload.get("soDienThoai");
            String anhDaiDien = (String) payload.get("anhDaiDien");
            java.sql.Date ngaySinh = payload.get("ngaySinh") != null ? java.sql.Date.valueOf((String) payload.get("ngaySinh")) : null;

            // Set dữ liệu vào đối tượng user
            if (hoTen != null) user.setHoTen(hoTen);
            if (gioiTinh != null) user.setGioiTinh(gioiTinh);
            if (soDienThoai != null) user.setSoDienThoai(soDienThoai);
            if (anhDaiDien != null) user.setAnhDaiDien(anhDaiDien);
            if (ngaySinh != null) user.setNgaySinh(new java.util.Date(ngaySinh.getTime()));

            // Cập nhật vào DB
            userService.updateUserProfile(user);

            return ResponseEntity.ok(Map.of("message", "Cập nhật profile thành công"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

}
