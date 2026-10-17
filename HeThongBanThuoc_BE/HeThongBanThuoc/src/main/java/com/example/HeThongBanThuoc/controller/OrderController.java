package com.example.HeThongBanThuoc.controller;

import com.example.HeThongBanThuoc.model.ChiTietDonHang;
import com.example.HeThongBanThuoc.model.CustomUserPrincipal;
import com.example.HeThongBanThuoc.model.DonHang;
import com.example.HeThongBanThuoc.model.DonHangChiTiet;
import com.example.HeThongBanThuoc.service.OrderService;
import com.example.HeThongBanThuoc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/donhang")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    /*
     * Lấy ID người dùng hiện tại từ JWT token
     */
    private Integer getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserPrincipal) {
            return ((CustomUserPrincipal) principal).getUserId();
        }
        throw new RuntimeException("Không xác định được người dùng");
    }

    /* API QUẢN LÝ & XEM ĐƠN HÀNG */

    /*
     * API: Lấy danh sách đơn hàng theo trạng thái
     * GET /api/user/donhang?trangThai=Tất cả
     * GET /api/user/donhang?trangThai=Đang xử lý
     * GET /api/user/donhang?trangThai=Đang giao
     * GET /api/user/donhang?trangThai=Đã giao
     * GET /api/user/donhang?trangThai=Đã hủy
     */
    @GetMapping
    public ResponseEntity<?> layDanhSachDonHang(
            @RequestParam(value = "trangThai", defaultValue = "Tất cả") String trangThai) {
        try {
            int maNguoiDung = getCurrentUserId();

            System.out.println("User " + maNguoiDung + " đang load đơn hàng - Trạng thái: " + trangThai);

            List<DonHangChiTiet> danhSach = orderService.layDanhSachDonHang(maNguoiDung, trangThai);

            return ResponseEntity.ok(danhSach);

        } catch (Exception e) {
            System.err.println("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /*
     * API: Thống kê số lượng đơn hàng theo trạng thái
     * GET /api/user/donhang/thongke
     * Response: { "Tất cả": 10, "Đang xử lý": 2, "Đang giao": 3, "Đã giao": 4, "Đã hủy": 1 }
     */
    @GetMapping("/thongke")
    public ResponseEntity<?> thongKeDonHang() {
        try {
            int maNguoiDung = getCurrentUserId();

            Map<String, Integer> thongKe = orderService.thongKeDonHangTheoTrangThai(maNguoiDung);

            return ResponseEntity.ok(thongKe);

        } catch (Exception e) {
            System.err.println("Lỗi khi thống kê đơn hàng: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /*
     * API: Lấy chi tiết đầy đủ của 1 đơn hàng
     * GET /api/user/donhang/{maDonHang}
     */
    @GetMapping("/{maDonHang}")
    public ResponseEntity<?> layChiTietDonHang(@PathVariable int maDonHang) {
        try {
            int maNguoiDung = getCurrentUserId();

            System.out.println("User " + maNguoiDung + " đang xem chi tiết đơn hàng #" + maDonHang);

            DonHangChiTiet chiTiet = orderService.layChiTietDonHangDayDu(maDonHang, maNguoiDung);

            return ResponseEntity.ok(chiTiet);

        } catch (Exception e) {
            System.err.println("Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /*
     * API: Tạo đơn hàng mới
     * POST /api/user/donhang/tao
     */
    @PostMapping("/addorder")
    public ResponseEntity<?> taoDonHang(@RequestBody DonHang donHang) {
        try {
            // Lấy mã người dùng từ token
            int maNguoiDung = getCurrentUserId();
            donHang.setMaNguoiDung(maNguoiDung);

            System.out.println("User " + maNguoiDung + " đang tạo đơn hàng. Tổng tiền: " + donHang.getTongTien());

            // Kiểm tra danh sách chi tiết
            if (donHang.getChiTietDonHangList() == null || donHang.getChiTietDonHangList().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Đơn hàng phải có ít nhất 1 sản phẩm!"
                ));
            }

            // Xử lý điểm thưởng
            if (donHang.isSuDungDiemThuong()) {
                int diemHienCo = userService.getDiemHienCo(maNguoiDung);
                if (diemHienCo > 0) {
                    donHang.setDiemDaDung(diemHienCo);
                    donHang.setGiamGia(BigDecimal.valueOf(diemHienCo));

                    double tongTien = donHang.getTongTien().doubleValue();
                    double tienPhaiTra = Math.max(0, tongTien - diemHienCo);
                    donHang.setTienPhaiTra(BigDecimal.valueOf(tienPhaiTra));
                }
            } else {
                donHang.setTienPhaiTra(donHang.getTongTien());
            }

            // Gọi method mới để tạo cả đơn hàng và chi tiết
            boolean result = orderService.taoDonHangVaChiTiet(donHang);

            if (!result) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Lỗi khi tạo đơn hàng!"
                ));
            }

            System.out.println("Đã tạo đơn hàng #" + donHang.getMaDonHang() +
                    " với " + donHang.getChiTietDonHangList().size() + " sản phẩm");

            // Trừ điểm nếu có sử dụng
            if (donHang.isSuDungDiemThuong() && donHang.getDiemDaDung() > 0) {
                boolean tru = userService.capNhatDiemTichLuy(maNguoiDung, -donHang.getDiemDaDung());
                if (!tru) {
                    System.out.println("Không thể trừ điểm thưởng cho người dùng " + maNguoiDung);
                }
            }

            // Cộng điểm mới sau khi thanh toán (2% tổng tiền phải trả)
            double tienPhaiTra = donHang.getTienPhaiTra().doubleValue();
            int diemCong = (int) (tienPhaiTra * 0.02);
            boolean cong = userService.capNhatDiemTichLuy(maNguoiDung, diemCong);
            if (!cong) {
                System.out.println("Không thể cộng điểm cho người dùng " + maNguoiDung);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tạo đơn hàng thành công. Dùng " + donHang.getDiemDaDung() +
                            " điểm, cộng thêm " + diemCong + " điểm tích lũy mới.",
                    "maDonHang", donHang.getMaDonHang(),
                    "soLuongSanPham", donHang.getChiTietDonHangList().size()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi tạo đơn hàng: " + e.getMessage()
            ));
        }
    }

    /*
     * API: Hủy đơn hàng
     * PUT /api/user/donhang/{maDonHang}/huy
     *
     * Chỉ cho phép hủy đơn hàng ở trạng thái "Đang xử lý"
     */
    @PutMapping("/{maDonHang}/huy")
    public ResponseEntity<?> huyDonHang(@PathVariable int maDonHang) {
        try {
            int maNguoiDung = getCurrentUserId();

            System.out.println("User " + maNguoiDung + " đang yêu cầu hủy đơn hàng #" + maDonHang);

            boolean result = orderService.huyDonHang(maDonHang, maNguoiDung);

            if (result) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Hủy đơn hàng thành công"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Không thể hủy đơn hàng. Đơn hàng không tồn tại hoặc không ở trạng thái 'Đang xử lý'"
                ));
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi hủy đơn hàng: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Lỗi khi hủy đơn hàng: " + e.getMessage()
            ));
        }
    }
}