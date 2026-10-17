package com.example.HeThongBanThuoc.controller;

import com.example.HeThongBanThuoc.model.DanhMuc;
import com.example.HeThongBanThuoc.service.CategoriService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/danhmuc")
public class CategoriControler {

    @Autowired
    private CategoriService categoriService;

    // API - lấy tất cả danh mục
    @GetMapping("/all")
    public ResponseEntity<List<DanhMuc>> getAllDanhMuc() {
        return ResponseEntity.ok(categoriService.getAllDanhMuc());
    }

    // API - lấy danh mục nổi bật (theo số lượng sản phẩm nhiều nhất)
    @GetMapping("/noibat")
    public ResponseEntity<List<DanhMuc>> getDanhMucNoiBat(
            @RequestParam(defaultValue = "6") int limit,
            @RequestParam(defaultValue = "5") int minSanPham  // Mặc định tối thiểu 5 sản phẩm
    ) {
        List<DanhMuc> list = categoriService.getDanhMucNoiBat(limit, minSanPham);
        return ResponseEntity.ok(list);
    }

    /*
     * API lấy danh mục con theo danh mục cha
     * @param id - MaDanhMucCha
     * @return List danh mục con
     */
    @GetMapping("/{id}/con")
    public ResponseEntity<?> getDanhMucCon(@PathVariable Integer id) {
        try {
            List<DanhMuc> danhMucCons = categoriService.getDanhMucConByMaCha(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", danhMucCons);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy danh mục con");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /*
     * API lấy chi tiết 1 danh mục
     * @param id - MaDanhMuc
     * @return Chi tiết danh mục
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDanhMucById(@PathVariable Integer id) {
        try {
            DanhMuc danhMuc = categoriService.getDanhMucById(id);

            if (danhMuc == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Không tìm thấy danh mục với ID: " + id);

                return ResponseEntity.status(404).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", danhMuc);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi lấy chi tiết danh mục");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
