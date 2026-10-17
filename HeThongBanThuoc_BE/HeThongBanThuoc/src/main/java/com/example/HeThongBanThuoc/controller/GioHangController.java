package com.example.HeThongBanThuoc.controller;

import com.example.HeThongBanThuoc.model.ChiTietGioHang;
import com.example.HeThongBanThuoc.model.GioHang;
import com.example.HeThongBanThuoc.service.ChiTietGioHangService;
import com.example.HeThongBanThuoc.service.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/GioHang")
public class GioHangController {

    @Autowired
    private GioHangService gioHangService;

    @Autowired
    private ChiTietGioHangService chiTietGioHangService;

    // API - Lấy tất cả giỏ hàng
    @GetMapping
    public ResponseEntity<List<GioHang>> getAll() {
        return ResponseEntity.ok(gioHangService.getAll());
    }

    // API - Lấy danh sách giỏ hàng của 1 người dùng
    @GetMapping("/{maNguoiDung}")
    public ResponseEntity<List<GioHang>> getByNguoiDung(@PathVariable int maNguoiDung) {
        return ResponseEntity.ok(gioHangService.getByNguoiDung(maNguoiDung));
    }

    // API - Xóa giỏ hàng
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        int result = gioHangService.delete(id);
        return result > 0
                ? ResponseEntity.ok("Xóa thành công")
                : ResponseEntity.badRequest().body("Xóa thất bại");
    }

    // API - Lấy hoặc tạo giỏ hàng của người dùng (1 người chỉ 1 giỏ 'Đang mở')
    @GetMapping("/by-user/{maNguoiDung}")
    public ResponseEntity<?> getOrCreateGioHangAndDetails(@PathVariable int maNguoiDung) {
        try {
            // Nếu chưa có → tạo mới
            GioHang gioHang = gioHangService.getOrCreateActiveCart(maNguoiDung);
            if (gioHang == null) {
                return ResponseEntity.status(500).body(Map.of("error", "Không thể tạo giỏ hàng mới"));
            }

            // Lấy danh sách chi tiết giỏ hàng (có ảnh sản phẩm)
            List<ChiTietGioHang> chiTiet = chiTietGioHangService.getByGioHang(gioHang.getMaGioHang());

            // Tạo phản hồi gửi về Android
            Map<String, Object> response = new HashMap<>();
            response.put("maGioHang", gioHang.getMaGioHang());
            response.put("maNguoiDung", gioHang.getMaNguoiDung());
            response.put("chiTiet", chiTiet); // Không tính tổng tiền

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

}
