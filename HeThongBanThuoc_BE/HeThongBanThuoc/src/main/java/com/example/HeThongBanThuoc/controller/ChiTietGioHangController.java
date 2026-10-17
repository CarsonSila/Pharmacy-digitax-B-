package com.example.HeThongBanThuoc.controller;

import com.example.HeThongBanThuoc.model.ChiTietGioHang;
import com.example.HeThongBanThuoc.service.ChiTietGioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/ChiTietGioHang")
public class ChiTietGioHangController {

    @Autowired
    private ChiTietGioHangService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // API - Lấy chi tiết giỏ hảng theo mã giỏ hàng
    @GetMapping("/{maGioHang}")
    public ResponseEntity<List<ChiTietGioHang>> getByGioHang(@PathVariable int maGioHang) {
        return ResponseEntity.ok(service.getByGioHang(maGioHang));
    }

    // API - Thêm sản phẩm vào giỏ
    @PostMapping("/add")
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            int maGioHang = (int) body.get("maGioHang");
            int maSanPham = (int) body.get("maSanPham");
            int soLuong = (int) body.get("soLuong");

            Double giaSanPham = getGiaSanPham(maSanPham);
            if (giaSanPham == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy sản phẩm có mã " + maSanPham));
            }

            double donGia = giaSanPham * soLuong;

            ChiTietGioHang dto = new ChiTietGioHang();
            dto.setMaGioHang(maGioHang);
            dto.setMaSanPham(maSanPham);
            dto.setSoLuong(soLuong);
            dto.setDonGia(donGia);

            int result = service.create(dto);
            return result > 0
                    ? ResponseEntity.ok(Map.of("message", "Đã thêm vào giỏ hàng thành công"))
                    : ResponseEntity.badRequest().body(Map.of("error", "Thêm thất bại"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // API - Xóa sản phẩm trong giỏ
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        int result = service.delete(id);
        return result > 0
                ? ResponseEntity.ok("Xóa thành công")
                : ResponseEntity.badRequest().body("Xóa thất bại");
    }

    // API - Tăng giảm số lượng sản phẩm dựa trên sản phẩm trong giỏ hàng
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateSoLuong(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            int soLuong = (int) body.get("soLuong");
            double donGia = Double.parseDouble(body.get("donGia").toString());
            String sql = "UPDATE ChiTietGioHang SET SoLuong = ?, DonGia = ? WHERE MaCTGH = ?";
            int rows = jdbcTemplate.update(sql, soLuong, donGia, id);
            return rows > 0
                    ? ResponseEntity.ok(Map.of("message", "Cập nhật thành công"))
                    : ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy sản phẩm"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Hàm truy vấn lấy giá sản phẩm
    private Double getGiaSanPham(int maSanPham) {
        String sql = "SELECT Gia FROM san_pham WHERE MaSanPham = ?";
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maSanPham);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("Gia");
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy giá sản phẩm: " + e.getMessage(), e);
        }
        return null;
    }
}
