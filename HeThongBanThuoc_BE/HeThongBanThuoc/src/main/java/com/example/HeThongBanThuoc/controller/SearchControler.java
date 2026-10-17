package com.example.HeThongBanThuoc.controller;

import com.example.HeThongBanThuoc.model.CustomUserPrincipal;
import com.example.HeThongBanThuoc.model.LichSuTimKiem;
import com.example.HeThongBanThuoc.model.SanPham;
import com.example.HeThongBanThuoc.model.TuKhoaTimKiem;
import com.example.HeThongBanThuoc.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/search")
public class SearchControler {

    @Autowired
    private SearchService searchService;

    /*
     * 1. Tìm kiếm sản phẩm (real-time) - YÊU CẦU ĐĂNG NHẬP
     */
    @GetMapping("/products")
    public ResponseEntity<?> searchSanPham(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "Gia") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        // Principal không thể null vì đã có @AuthenticationPrincipal + Security filter
        Integer userId = principal.getUserId();

        List<SanPham> result = searchService.searchSanPham(keyword, page, size, sortBy, sortDir, userId);

        if (result.isEmpty()) {
            return ResponseEntity.ok().body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy sản phẩm nào!",
                    "data", List.of()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result,
                "total", result.size()
        ));
    }

    /*
     * 2. Lấy top từ khóa phổ biến (cho "Tra cứu hàng đầu") - YÊU CẦU ĐĂNG NHẬP
     * GET /api/user/search/trending?limit=8
     */
    @GetMapping("/trending")
    public ResponseEntity<?> getTrendingKeywords(
            @RequestParam(defaultValue = "8") int limit
    ) {
        List<TuKhoaTimKiem> trending = searchService.getTrendingKeywords(limit);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", trending
        ));
    }

    /*
     * 3. Lấy lịch sử tìm kiếm của user - YÊU CẦU ĐĂNG NHẬP
     * GET /api/user/search/history?limit=10
     */
    @GetMapping("/history")
    public ResponseEntity<?> getLichSuTimKiem(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Integer userId = principal.getUserId();
        List<LichSuTimKiem> history = searchService.getLichSuTimKiem(userId, limit);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", history
        ));
    }

    /*
     * 4. Xóa 1 item lịch sử - YÊU CẦU ĐĂNG NHẬP
     * DELETE /api/user/search/history/{maLichSu}
     */
    @DeleteMapping("/history/{maLichSu}")
    public ResponseEntity<?> xoaLichSu(
            @PathVariable Integer maLichSu,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Integer userId = principal.getUserId();
        boolean success = searchService.xoaLichSu(maLichSu, userId);

        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Đã xóa lịch sử!" : "Không tìm thấy hoặc không có quyền xóa!"
        ));
    }

    /*
     * 5. Xóa tất cả lịch sử - YÊU CẦU ĐĂNG NHẬP
     * DELETE /api/user/search/history/clear
     */
    @DeleteMapping("/history/clear")
    public ResponseEntity<?> xoaTatCaLichSu(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Integer userId = principal.getUserId();
        boolean success = searchService.xoaTatCaLichSu(userId);

        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Đã xóa tất cả lịch sử!" : "Lỗi xảy ra!"
        ));
    }

    /*
     * 6. Lấy sản phẩm thay thế (khi sản phẩm hết hàng) - YÊU CẦU ĐĂNG NHẬP
     * GET /api/user/search/related-products?maSanPham=123
     */
    @GetMapping("/related-products")
    public ResponseEntity<?> getSanPhamThayThe(
            @RequestParam Integer maSanPham
    ) {
        List<SanPham> result = searchService.getSanPhamThayThe(maSanPham);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", result,
                "message", result.isEmpty() ? "Không có sản phẩm thay thế" : "Tìm thấy " + result.size() + " sản phẩm thay thế"
        ));
    }
}