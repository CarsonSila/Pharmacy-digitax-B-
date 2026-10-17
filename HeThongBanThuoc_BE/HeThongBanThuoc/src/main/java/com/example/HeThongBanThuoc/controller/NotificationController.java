package com.example.HeThongBanThuoc.controller;

import com.example.HeThongBanThuoc.model.ThongBao;
import com.example.HeThongBanThuoc.service.NotificationService;
import com.example.HeThongBanThuoc.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/thongbao")
public class NotificationController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private NotificationService notificationService;

    // Helper method: Lấy userId từ token
    private int getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "");
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        throw new RuntimeException("Token không hợp lệ");
    }

    // API 1: Lấy danh sách thông báo của user (phân trang)
    @GetMapping
    public ResponseEntity<?> getThongBaoByUser(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            int userId = getUserIdFromRequest(request);
            List<ThongBao> result = notificationService.getThongBaoByUser(userId, page, size);
            int total = notificationService.countTotalThongBao(userId);
            int unread = notificationService.countUnread(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("pagination", Map.of(
                    "page", page,
                    "size", size,
                    "total", total,
                    "unread", unread
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 2: Đếm số thông báo chưa đọc
    @GetMapping("/unread-count")
    public ResponseEntity<?> countUnread(HttpServletRequest request) {
        try {
            int userId = getUserIdFromRequest(request);
            int count = notificationService.countUnread(userId);
            return ResponseEntity.ok(Map.of("success", true, "unreadCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 3: Đánh dấu một thông báo đã đọc
    @PutMapping("/{notificationId}/mark-as-read")
    public ResponseEntity<?> markAsRead(
            HttpServletRequest request,
            @PathVariable int notificationId
    ) {
        try {
            int userId = getUserIdFromRequest(request);
            boolean success = notificationService.markAsRead(userId, notificationId);
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Đã đánh dấu đã đọc"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy thông báo"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 4: Đánh dấu tất cả thông báo là đã đọc
    @PutMapping("/mark-all-as-read")
    public ResponseEntity<?> markAllAsRead(HttpServletRequest request) {
        try {
            int userId = getUserIdFromRequest(request);
            int updated = notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(Map.of("success", true, "updatedCount", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 5: Lấy thông báo theo loại
    @GetMapping("/loai/{loaiThongBao}")
    public ResponseEntity<?> getThongBaoByLoai(
            HttpServletRequest request,
            @PathVariable String loaiThongBao,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            int userId = getUserIdFromRequest(request);
            List<ThongBao> result = notificationService.getThongBaoByLoai(userId, loaiThongBao, page, size);
            int count = notificationService.countByLoai(userId, loaiThongBao);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result,
                    "total", count
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 6: Lấy chi tiết 1 thông báo
    @GetMapping("/{notificationId}")
    public ResponseEntity<?> getThongBaoById(
            HttpServletRequest request,
            @PathVariable int notificationId
    ) {
        try {
            int userId = getUserIdFromRequest(request);
            ThongBao thongBao = notificationService.getThongBaoById(userId, notificationId);

            if (thongBao != null) {
                notificationService.markAsRead(userId, notificationId);
                return ResponseEntity.ok(Map.of("success", true, "data", thongBao));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy thông báo"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 7: Tạo thông báo mới (Admin)
    @PostMapping("/create")
    public ResponseEntity<?> createThongBao(@RequestBody ThongBao thongBao) {
        try {
            boolean success = notificationService.createThongBao(thongBao);
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Tạo thông báo thành công"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Tạo thông báo thất bại"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 8: Xóa 1 thông báo
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteThongBao(
            HttpServletRequest request,
            @PathVariable int notificationId
    ) {
        try {
            int userId = getUserIdFromRequest(request);
            boolean success = notificationService.deleteThongBao(userId, notificationId);

            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Xóa thành công"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy thông báo"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 9: Xóa tất cả thông báo đã đọc
    @DeleteMapping("/delete-all-read")
    public ResponseEntity<?> deleteAllRead(HttpServletRequest request) {
        try {
            int userId = getUserIdFromRequest(request);
            int deleted = notificationService.deleteAllRead(userId);
            return ResponseEntity.ok(Map.of("success", true, "deletedCount", deleted));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 10: Lấy thông báo chưa đọc
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(HttpServletRequest request) {
        try {
            int userId = getUserIdFromRequest(request);
            List<ThongBao> result = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(Map.of("success", true, "data", result, "count", result.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 11: Tìm kiếm thông báo theo từ khóa
    @GetMapping("/search")
    public ResponseEntity<?> searchThongBao(
            HttpServletRequest request,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            int userId = getUserIdFromRequest(request);
            List<ThongBao> result = notificationService.searchThongBao(userId, keyword, page, size);
            return ResponseEntity.ok(Map.of("success", true, "data", result, "keyword", keyword));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 12: Lấy thông báo mới nhất
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestThongBao(
            HttpServletRequest request,
            @RequestParam(defaultValue = "5") int limit
    ) {
        try {
            int userId = getUserIdFromRequest(request);
            List<ThongBao> result = notificationService.getLatestThongBao(userId, limit);
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 13: Kiểm tra có thông báo mới kể từ lần kiểm tra trước
    @GetMapping("/has-new")
    public ResponseEntity<?> hasNewNotifications(
            HttpServletRequest request,
            @RequestParam String lastCheckTime
    ) {
        try {
            int userId = getUserIdFromRequest(request);
            boolean hasNew = notificationService.hasNewNotifications(userId, lastCheckTime);
            return ResponseEntity.ok(Map.of("success", true, "hasNew", hasNew));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 14: Lấy thống kê thông báo theo loại
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(HttpServletRequest request) {
        try {
            int userId = getUserIdFromRequest(request);
            List<Object[]> stats = notificationService.getThongKeTheoLoai(userId);

            Map<String, Map<String, Integer>> result = new HashMap<>();
            for (Object[] row : stats) {
                String loai = (String) row[0];
                Integer soLuong = (Integer) row[1];
                Integer chuaDoc = (Integer) row[2];

                result.put(loai, Map.of("total", soLuong, "unread", chuaDoc));
            }

            return ResponseEntity.ok(Map.of("success", true, "statistics", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 15: Xóa tất cả thông báo
    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllThongBao(HttpServletRequest request) {
        try {
            int userId = getUserIdFromRequest(request);
            int deleted = notificationService.deleteAllThongBao(userId);
            return ResponseEntity.ok(Map.of("success", true, "deletedCount", deleted));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 16: Gửi thông báo broadcast cho nhiều user (Admin)
    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcastNotification(@RequestBody Map<String, Object> requestBody) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> userIds = (List<Integer>) requestBody.get("userIds");
            String tieuDe = (String) requestBody.get("tieuDe");
            String noiDung = (String) requestBody.get("noiDung");
            String loaiThongBao = (String) requestBody.get("loaiThongBao");

            int count = notificationService.createBroadcastNotification(userIds, tieuDe, noiDung, loaiThongBao);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã gửi thông báo tới " + count + " người dùng"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    // API 17: Lấy thông báo theo khoảng thời gian
    @GetMapping("/date-range")
    public ResponseEntity<?> getThongBaoByDateRange(
            HttpServletRequest request,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        try {
            int userId = getUserIdFromRequest(request);
            List<ThongBao> result = notificationService.getThongBaoByDateRange(userId, startDate, endDate);
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
}
