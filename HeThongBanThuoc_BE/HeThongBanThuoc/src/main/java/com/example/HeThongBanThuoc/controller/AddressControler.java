package com.example.HeThongBanThuoc.controller;

import com.example.HeThongBanThuoc.model.CustomUserPrincipal;
import com.example.HeThongBanThuoc.model.DiaChi;
import com.example.HeThongBanThuoc.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/diachi")
public class AddressControler {

    @Autowired
    private AddressService addressService;  // Service xử lý business logic liên quan địa chỉ

    /*
        - Nghiệp vụ xử lý:
        - user id sẽ dựa vào bảo mật JWT -> nếu JWWT giả mạo sẽ không pass
    */

    // Lấy ID người dùng hiện tại từ SecurityContext (JWT đã xác thực)
    private Integer getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) principal).getUserId(); // Lấy userId từ principal
        }
        throw new RuntimeException("Không xác định được người dùng");
    }

    // API: Lấy danh sách địa chỉ
    @GetMapping
    public ResponseEntity<?> getDanhSachDiaChi() {
        try {
            int maNguoiDung = getCurrentUserId();
            List<DiaChi> list = addressService.layDanhSachDiaChiTheoNguoiDung(maNguoiDung);
            return ResponseEntity.ok(list); // trả về danh sách địa chỉ
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // API: Thêm địa chỉ
    @PostMapping
    public ResponseEntity<?> themDiaChi(@RequestBody DiaChi diaChi) {
        try {
            int maNguoiDung = getCurrentUserId();
            diaChi.setMaNguoiDung(maNguoiDung);  // Gắn địa chỉ cho user hiện tại
            int idMoi = addressService.themDiaChi(diaChi);
            return ResponseEntity.ok(Map.of(
                    "message", "Thêm địa chỉ thành công",
                    "id", String.valueOf(idMoi)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // API: Xóa địa chỉ
    @DeleteMapping("/{id}")
    public ResponseEntity<?> xoaDiaChi(@PathVariable("id") int id) {
        try {
            int maNguoiDung = getCurrentUserId();
            addressService.xoaDiaChi(id, maNguoiDung); // Chỉ xóa nếu địa chỉ thuộc user
            return ResponseEntity.ok(Map.of("message", "Xóa địa chỉ thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /*
        - Nghiệp vụ xử lý:
        - Transaction rollback nếu lỗi → bảo vệ tính nhất quán.
        - Nếu 2 request update địa chỉ mặc định cùng lúc → có thể xung đột.
     */
    // API: Đặt địa chỉ mặc định
    @PutMapping("/{id}/default")
    public ResponseEntity<?> datMacDinh(@PathVariable("id") int id) {
        try {
            int maNguoiDung = getCurrentUserId();
            addressService.datDiaChiMacDinh(maNguoiDung, id); // Service tự rollback nếu lỗi
            return ResponseEntity.ok(Map.of("message", "Cập nhật địa chỉ mặc định thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // API: Cập nhật địa chỉ
    @PutMapping
    public ResponseEntity<?> capNhatDiaChi(@RequestBody DiaChi diaChi) {
        try {
            int maNguoiDung = getCurrentUserId();
            diaChi.setMaNguoiDung(maNguoiDung);

            if (diaChi.getMaDiaChi() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Chưa có ID địa chỉ"));
            }

            addressService.capNhatDiaChi(diaChi);

            // Nếu là địa chỉ mặc định, cập nhật
            if (diaChi.getDiaChiMacDinh() != null && diaChi.getDiaChiMacDinh()) {
                addressService.datDiaChiMacDinh(maNguoiDung, diaChi.getMaDiaChi());
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật địa chỉ thành công",
                    "id", String.valueOf(diaChi.getMaDiaChi())
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
