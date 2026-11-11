package com.example.HeThongBanThuoc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * FIX #27: Global exception handler.
 * Ngăn stack trace lộ ra ngoài qua HTTP response.
 * Tất cả lỗi chưa được xử lý sẽ trả về JSON thân thiện.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Xử lý các RuntimeException (lỗi nghiệp vụ đã biết: email trùng, sai mật khẩu, v.v.)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        // Log ở level WARN — đây là lỗi có kiểm soát
        log.warn("Business exception: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * Xử lý IllegalArgumentException (input không hợp lệ)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * Xử lý tất cả exception chưa được bắt — trả về 500 với thông báo chung,
     * không tiết lộ chi tiết nội bộ cho client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception e) {
        // Log ở level ERROR với full stack trace cho dev/ops
        log.error("Unhandled exception: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."));
    }
}
