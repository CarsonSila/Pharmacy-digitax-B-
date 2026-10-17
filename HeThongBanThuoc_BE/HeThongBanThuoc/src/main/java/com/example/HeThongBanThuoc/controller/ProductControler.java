package com.example.HeThongBanThuoc.controller;

import com.example.HeThongBanThuoc.model.SanPham;
import com.example.HeThongBanThuoc.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sanpham")
public class ProductControler {
    @Autowired
    private ProductService productService;

    // API - Lấy sản phẩm bán chạy
    @GetMapping("/best-sellers")
    public ResponseEntity<List<SanPham>> getBestSellingProducts(
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<SanPham> products = productService.getBestSellingProducts(limit);
        return ResponseEntity.ok(products);
    }

    // API - Lấy tất cả sản phẩm
    @GetMapping("/all")
    public ResponseEntity<List<SanPham>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /*
     * API - Lấy sản phẩm theo danh mục
     * GET /api/sanpham/danhmuc/{id}
     *
     * @param id - MaDanhMuc (bao gồm cả sản phẩm của danh mục con)
     * @return List sản phẩm
     */
    @GetMapping("/danhmuc/{id}")
    public ResponseEntity getSanPhamByDanhMuc(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "ban-chay") String sort // ← THÊM THAM SỐ NÀY
    ) {
        try {
            List<SanPham> sanPhams = productService.getSanPhamByDanhMuc(id, sort);

            Map response = new HashMap<>();
            response.put("success", true);
            response.put("data", sanPhams);
            response.put("total", sanPhams.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy sản phẩm theo danh mục");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /*
     * API - Lấy chi tiết 1 sản phẩm
     * GET /api/sanpham/{id}
     *
     * @param id - MaSanPham
     * @return Chi tiết sản phẩm
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSanPhamById(@PathVariable Integer id) {
        try {
            SanPham sanPham = productService.getSanPhamById(id);

            if (sanPham == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Không tìm thấy sản phẩm với ID: " + id);

                return ResponseEntity.status(404).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", sanPham);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy chi tiết sản phẩm");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
