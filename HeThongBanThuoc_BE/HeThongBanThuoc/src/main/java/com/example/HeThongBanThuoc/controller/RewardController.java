package com.example.HeThongBanThuoc.controller;

import com.example.HeThongBanThuoc.model.*;
import com.example.HeThongBanThuoc.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*") // Cho phép truy cập từ các domain khác
public class RewardController {
    @Autowired
    private RewardService rewardService;

    // 1. API -  Lấy số điểm hiện tại thông qua userid
    @GetMapping("/rewards/balance/{userId}")
    public ResponseEntity<?> getDiemBalance(@PathVariable Integer userId) {
        try {
            Map<String, Object> balance = rewardService.getDiemBalance(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", balance);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 2. API - Lịch sử tích/tiêu điểm dựa trên id người dùng
    @GetMapping("/rewards/history/{userId}")
    public ResponseEntity<?> getLichSuDiem(@PathVariable Integer userId) {
        try {
            List<DiemThuong> history = rewardService.getLichSuDiem(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", history);
            response.put("total", history.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 3. API - Danh sách quà tặng (có filter)
    @GetMapping("/gifts")
    public ResponseEntity<?> getDanhSachQuaTang(
            @RequestParam(required = false) Integer diem) {
        try {
            List<QuaTang> gifts = rewardService.getDanhSachQuaTang(diem);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", gifts);
            response.put("total", gifts.size());

            if (diem != null) {
                response.put("filter", Map.of("diem", diem));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 4. API - Đổi quà (nhận JSON body hoặc form)
    @PostMapping("/gifts/redeem")
    public ResponseEntity<?> doiQua(@RequestBody Map<String, Object> payload) {
        try {
            // Parse parameters từ JSON
            Integer maNguoiDung = (Integer) payload.get("maNguoiDung");
            Integer maQuaTang = (Integer) payload.get("maQuaTang");
            Integer soLuong = payload.get("soLuong") != null ? (Integer) payload.get("soLuong") : 1;
            String ghiChu = (String) payload.get("ghiChu");

            // Validate
            if (maNguoiDung == null || maQuaTang == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Thiếu thông tin bắt buộc: maNguoiDung, maQuaTang");
                return ResponseEntity.badRequest().body(error);
            }

            // Gọi service
            Map<String, Object> result = rewardService.doiQua(maNguoiDung, maQuaTang, soLuong, ghiChu);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đổi quà thành công! Vui lòng chờ xử lý.");
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 5. API - Lịch sử đổi quà
    @GetMapping("/gifts/history/{userId}")
    public ResponseEntity<?> getLichSuDoiQua(@PathVariable Integer userId) {
        try {
            List<LichSuDoiQua> history = rewardService.getLichSuDoiQua(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", history);
            response.put("total", history.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 6. API - Chi tiết quà tặng
    @GetMapping("/gifts/{giftId}")
    public ResponseEntity<?> getChiTietQuaTang(@PathVariable Integer giftId) {
        try {
            List<QuaTang> gifts = rewardService.getDanhSachQuaTang(null);
            QuaTang gift = gifts.stream()
                    .filter(q -> q.getMaQuaTang().equals(giftId))
                    .findFirst()
                    .orElse(null);

            if (gift == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Không tìm thấy quà tặng");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", gift);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // 7. Health Check
    @GetMapping("/rewards/ping")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Reward API is running");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
