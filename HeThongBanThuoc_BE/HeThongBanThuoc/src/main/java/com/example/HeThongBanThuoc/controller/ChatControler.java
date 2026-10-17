package com.example.HeThongBanThuoc.controller;

import com.example.HeThongBanThuoc.model.CustomUserPrincipal;
import com.example.HeThongBanThuoc.model.TinNhan;
import com.example.HeThongBanThuoc.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.sql.Connection;

/*
 * REST Controller xử lý chat với AI
 * Yêu cầu: Người dùng đã đăng nhập (ROLE_USER)
 * Nghiệp vụ tổng quan:
 * 1. FE gửi request kèm JWT
 * 2. BE xác thực JWT → lấy CustomUserPrincipal
 * 3. Controller dùng userId từ principal thao tác dữ liệu liên quan user
 * 4. Tương tác với ChatService:
 *      - Lưu tin nhắn user
 *      - Gửi câu hỏi đến AI (Gemini API)
 *      - Lưu phản hồi AI
 * 5. Trả response chuẩn về FE (JSON hoặc SSE)
 */
@RestController
@RequestMapping("/api/user/chat")
@PreAuthorize("hasAnyAuthority('ROLE_USER')")
public class ChatControler {

    @Autowired
    private ChatService chatService;

    // Lấy userId từ JWT token hiện tại, dùng cho tất cả nghiệp vụ liên quan user
    private Integer getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) principal).getUserId();
        }
        throw new RuntimeException("Không xác định được người dùng");
    }

    // Tạo response lỗi chuẩn
    private ResponseEntity<Map<String, String>> errorResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(Map.of("error", message));
    }

    // Tạo response thành công chuẩn
    private ResponseEntity<Map<String, String>> successResponse(String message) {
        return ResponseEntity.ok(Map.of("message", message));
    }

    /*
     * Gửi tin nhắn → nhận phản hồi đồng bộ
     * FE POST /api/user/chat/send { "message": "..." }
     * Quy trình nghiệp vụ:
     * 1. Validate message không rỗng
     * 2. Lấy hoặc tạo phiên tư vấn hiện tại
     * 3. Lưu tin nhắn user vào DB
     * 4. Gọi Gemini API lấy phản hồi AI
     * 5. Lưu phản hồi AI vào DB
     * 6. Trả về JSON chuẩn cho FE
     */

    @PostMapping("/send")
    public ResponseEntity<?> guiTinNhan(@RequestBody Map<String, String> request) {
        try {
            // Validate input
            String cauHoi = request.get("message");
            if (cauHoi == null || cauHoi.trim().isEmpty()) {
                return errorResponse("Tin nhắn không được để trống", HttpStatus.BAD_REQUEST);
            }

            // Lấy hoặc tạo phiên tư vấn
            int maNguoiDung = getCurrentUserId();
            int maPhienTuVan = chatService.layHoacTaoPhienTuVan(maNguoiDung);

            // Lưu tin nhắn người dùng
            String thoiGianHienTai = LocalDateTime.now().toString();
            TinNhan userMsg = new TinNhan(maPhienTuVan, "Khách hàng", cauHoi, thoiGianHienTai);
            chatService.luuTinNhan(userMsg);

            // Gọi AI
            String aiResponse = chatService.guiCauHoiToiAI(cauHoi, maPhienTuVan);

            // Xử lý trường hợp AI không trả lời
            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                aiResponse = "Xin lỗi, hiện tôi đang quá tải. Vui lòng thử lại sau.";
            }

            // Lưu phản hồi AI
            TinNhan aiMsg = new TinNhan(maPhienTuVan, "AI", aiResponse, LocalDateTime.now().toString());
            chatService.luuTinNhan(aiMsg);

            // Trả về response
            return ResponseEntity.ok(Map.of(
                    "maPhienTuVan", maPhienTuVan,
                    "userMessage", Map.of(
                            "content", cauHoi,
                            "type", "Khách hàng",
                            "time", thoiGianHienTai
                    ),
                    "aiMessage", Map.of(
                            "content", aiResponse,
                            "type", "AI",
                            "time", LocalDateTime.now().toString()
                    )
            ));

        } catch (Exception e) {
            return errorResponse("Lỗi khi gọi AI: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
     * Stream phản hồi AI theo thời gian thực (SSE)
     * FE POST /api/user/chat/stream { "message": "..." }
     * Quy trình nghiệp vụ:
     * 1. Validate message không rỗng
     * 2. Lấy hoặc tạo phiên tư vấn hiện tại
     * 3. Lưu tin nhắn user
     * 4. Gọi service stream AI → gửi SSE từng token về FE
     * 5. Lưu phản hồi AI đầy đủ vào DB khi kết thúc
     */
    @PostMapping(
            value = "/stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter streamChat(@RequestBody Map<String, String> request) {
        try {
            // Validate input
            String cauHoi = request.get("message");
            if (cauHoi == null || cauHoi.trim().isEmpty()) {
                throw new IllegalArgumentException("Tin nhắn không được trống");
            }

            // Lấy hoặc tạo phiên tư vấn
            int maNguoiDung = getCurrentUserId();
            int maPhienTuVan = chatService.layHoacTaoPhienTuVan(maNguoiDung);

            // Lưu tin nhắn người dùng
            TinNhan userMsg = new TinNhan(
                    maPhienTuVan,
                    "Khách hàng",
                    cauHoi,
                    LocalDateTime.now().toString()
            );
            chatService.luuTinNhan(userMsg);

            // Gọi service để stream response
            return chatService.streamAIResponse(cauHoi, maPhienTuVan);

        } catch (Exception e) {
            // Trả về SSE emitter với lỗi
            return taoSSEEmitterLoi("Lỗi: " + e.getMessage());
        }
    }

    /*
     * Tạo SSE emitter để gửi lỗi và đóng stream
     */
    private SseEmitter taoSSEEmitterLoi(String message) {
        SseEmitter emitter = new SseEmitter();
        try {
            emitter.send(SseEmitter.event().name("error").data(message));
            emitter.complete();
        } catch (IOException ignored) {
            emitter.completeWithError(new RuntimeException(message));
        }
        return emitter;
    }

    // Hám thiết lập interface API - Quản lý & lưu trữ phiên tư vấn

    /*
     * Lấy phiên chat hiện tại (đang mở) với lịch sử tin nhắn
     * GET /api/user/chat/current-session
     * @return JSON chứa maPhienTuVan + danh sách tin nhắn
     */
    @GetMapping("/current-session")
    public ResponseEntity<?> layPhienHienTai() {
        try {
            int maNguoiDung = getCurrentUserId();
            int maPhienTuVan = chatService.layHoacTaoPhienTuVan(maNguoiDung);
            List<TinNhan> lichSu = chatService.layLichSuTinNhan(maPhienTuVan, 50);

            return ResponseEntity.ok(Map.of(
                    "maPhienTuVan", maPhienTuVan,
                    "tinNhan", lichSu
            ));
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
     * Lấy danh sách tất cả phiên tư vấn của người dùng
     * GET /api/user/chat/sessions
     * @return JSON chứa danh sách phiên + tổng số
     */
    @GetMapping("/sessions")
    public ResponseEntity<?> layDanhSachPhienTuVan() {
        try {
            int maNguoiDung = getCurrentUserId();
            List<Map<String, Object>> danhSach = chatService.layDanhSachPhienTuVan(maNguoiDung);

            return ResponseEntity.ok(Map.of(
                    "sessions", danhSach,
                    "total", danhSach.size()
            ));
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
     * Lấy lịch sử tin nhắn của một phiên cụ thể
     * GET /api/user/chat/history/{maPhienTuVan}?limit=50
     * @param maPhienTuVan ID phiên tư vấn
     * @param limit Số lượng tin nhắn tối đa (mặc định 50)
     * @return JSON chứa danh sách tin nhắn
     */
    @GetMapping("/history/{maPhienTuVan}")
    public ResponseEntity<?> layLichSuTinNhan(
            @PathVariable int maPhienTuVan,
            @RequestParam(defaultValue = "50") int limit
    ) {
        try {
            List<TinNhan> lichSu = chatService.layLichSuTinNhan(maPhienTuVan, limit);

            return ResponseEntity.ok(Map.of(
                    "maPhienTuVan", maPhienTuVan,
                    "tinNhan", lichSu
            ));
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
     * Kết thúc phiên tư vấn (đánh dấu trạng thái)
     * PUT /api/user/chat/end/{maPhienTuVan}
     * @param maPhienTuVan ID phiên cần kết thúc
     * @return Thông báo thành công
     */
    @PutMapping("/end/{maPhienTuVan}")
    public ResponseEntity<?> ketThucPhienTuVan(@PathVariable int maPhienTuVan) {
        try {
            int maNguoiDung = getCurrentUserId();
            chatService.ketThucPhienTuVan(maPhienTuVan, maNguoiDung);

            return successResponse("Đã kết thúc phiên tư vấn");
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
     * Xóa phiên tư vấn (cascade xóa cả tin nhắn)
     * DELETE /api/user/chat/session/{maPhienTuVan}
     * @param maPhienTuVan ID phiên cần xóa
     * @return Thông báo thành công
     */
    @DeleteMapping("/session/{maPhienTuVan}")
    public ResponseEntity<?> xoaPhienTuVan(@PathVariable int maPhienTuVan) {
        try {
            int maNguoiDung = getCurrentUserId();
            chatService.xoaPhienTuVan(maPhienTuVan, maNguoiDung);

            return successResponse("Đã xóa phiên tư vấn");
        } catch (Exception e) {
            return errorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
     * Tạo phiên tư vấn mới (không quan tâm phiên hiện tại)
     * POST /api/user/chat/new-session
     * @return JSON chứa maPhienTuVan mới
     */
    @PostMapping("/new-session")
    public ResponseEntity<?> taoPhienTuVanMoi() {
        try {
            int maNguoiDung = getCurrentUserId();
            int maPhienTuVanMoi;
            try (Connection conn = chatService.getDataSource().getConnection()) {
                maPhienTuVanMoi = chatService.taoPhienTuVanMoi(conn, maNguoiDung);
            }

            return ResponseEntity.ok(Map.of(
                    "maPhienTuVan", maPhienTuVanMoi,
                    "message", "Đã tạo phiên tư vấn mới"
            ));
        } catch (Exception e) {
            return errorResponse("Lỗi khi tạo phiên mới: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}