package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.model.ChiTietDonHang;
import com.example.HeThongBanThuoc.model.DonHang;
import com.example.HeThongBanThuoc.model.DonHangChiTiet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private DataSource dataSource;

    /* API ĐỌC/XEM ĐƠN HÀNG
    /*
     * Lấy danh sách đơn hàng theo người dùng và trạng thái
     * @param maNguoiDung ID người dùng
     * @param trangThai Trạng thái đơn hàng ("Tất cả", "Đang xử lý", "Đang giao", "Đã giao", "Đã hủy")
     * @return Danh sách đơn hàng kèm chi tiết sản phẩm
     */
    public List<DonHangChiTiet> layDanhSachDonHang(int maNguoiDung, String trangThai) {
        List<DonHangChiTiet> danhSachDonHang = new ArrayList<>();

        String sqlDonHang = buildQueryDonHang(trangThai);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlDonHang)) {

            ps.setInt(1, maNguoiDung);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DonHangChiTiet donHang = new DonHangChiTiet();
                    donHang.setMaDonHang(rs.getInt("MaDonHang"));
                    donHang.setNgayTao(rs.getTimestamp("NgayTao"));
                    donHang.setTrangThai(rs.getString("TrangThai"));
                    donHang.setTongTien(rs.getBigDecimal("TongTien"));
                    donHang.setGiamGia(rs.getBigDecimal("GiamGia"));
                    donHang.setTienPhaiTra(rs.getBigDecimal("TienPhaiTra"));

                    danhSachDonHang.add(donHang);
                }
            }

            // Load chi tiết sản phẩm cho từng đơn hàng
            for (DonHangChiTiet donHang : danhSachDonHang) {
                List<ChiTietDonHang> chiTietList = layChiTietDonHang(donHang.getMaDonHang());
                donHang.setDanhSachSanPham(chiTietList);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage(), e);
        }

        return danhSachDonHang;
    }

    /*
     * Lấy chi tiết sản phẩm của đơn hàng
     * @param maDonHang ID đơn hàng
     * @return Danh sách sản phẩm trong đơn
     */
    private List<ChiTietDonHang> layChiTietDonHang(int maDonHang) {
        List<ChiTietDonHang> chiTietList = new ArrayList<>();

        String sql = "SELECT ct.MaChiTiet, ct.MaDonHang, ct.MaSanPham, ct.SoLuong, ct.DonGia, ct.ThanhTien, " +
                "sp.TenThuoc, asp.DuongDan AS HinhAnh " +
                "FROM ChiTietDonHang ct " +
                "JOIN san_pham sp ON ct.MaSanPham = sp.MaSanPham " +
                "LEFT JOIN AnhSanPham asp ON sp.MaSanPham = asp.MaSanPham AND asp.AnhChinh = 1 " +
                "WHERE ct.MaDonHang = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maDonHang);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietDonHang chiTiet = new ChiTietDonHang();
                    chiTiet.setMaChiTiet(rs.getInt("MaChiTiet"));
                    chiTiet.setMaDonHang(rs.getInt("MaDonHang"));
                    chiTiet.setMaSanPham(rs.getInt("MaSanPham"));
                    chiTiet.setSoLuong(rs.getInt("SoLuong"));
                    chiTiet.setDonGia(rs.getBigDecimal("DonGia"));
                    chiTiet.setThanhTien(rs.getBigDecimal("ThanhTien"));
                    chiTiet.setTenThuoc(rs.getString("TenThuoc"));
                    chiTiet.setHinhAnh(rs.getString("HinhAnh"));

                    chiTietList.add(chiTiet);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage(), e);
        }

        return chiTietList;
    }

    /*
     * Build câu query SQL theo trạng thái
     */
    private String buildQueryDonHang(String trangThai) {
        String baseQuery = "SELECT MaDonHang, MaNguoiDung, TongTien, GiamGia, TienPhaiTra, TrangThai, NgayTao " +
                "FROM DonHang " +
                "WHERE MaNguoiDung = ? ";

        if (trangThai != null && !trangThai.equals("Tất cả")) {
            baseQuery += "AND TrangThai = '" + trangThai + "' ";
        }

        baseQuery += "ORDER BY NgayTao DESC";

        return baseQuery;
    }

    /*
     * Lấy thống kê số lượng đơn hàng theo trạng thái
     * @param maNguoiDung ID người dùng
     * @return Map<TrangThai, SoLuong>
     */
    public Map<String, Integer> thongKeDonHangTheoTrangThai(int maNguoiDung) {
        Map<String, Integer> thongKe = new HashMap<>();

        String sql = "SELECT TrangThai, COUNT(*) as SoLuong " +
                "FROM DonHang " +
                "WHERE MaNguoiDung = ? " +
                "GROUP BY TrangThai";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maNguoiDung);

            try (ResultSet rs = ps.executeQuery()) {
                thongKe.put("Đang xử lý", 0);
                thongKe.put("Đang giao", 0);
                thongKe.put("Đá giao", 0);
                thongKe.put("Đã hủy", 0);

                int tongTatCa = 0;
                while (rs.next()) {
                    String trangThai = rs.getString("TrangThai");
                    int soLuong = rs.getInt("SoLuong");
                    thongKe.put(trangThai, soLuong);
                    tongTatCa += soLuong;
                }
                thongKe.put("Tất cả", tongTatCa);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thống kê đơn hàng: " + e.getMessage(), e);
        }

        return thongKe;
    }

    /*
     * Lấy chi tiết đầy đủ của 1 đơn hàng
     * @param maDonHang ID đơn hàng
     * @param maNguoiDung ID người dùng (để verify quyền truy cập)
     * @return Chi tiết đơn hàng đầy đủ bao gồm thông tin người nhận, địa chỉ
     */
    public DonHangChiTiet layChiTietDonHangDayDu(int maDonHang, int maNguoiDung) {
        DonHangChiTiet chiTiet = new DonHangChiTiet();

        String sql = "SELECT " +
                "dh.MaDonHang, dh.NgayTao, dh.TrangThai, dh.TongTien, dh.GiamGia, dh.TienPhaiTra, " +
                "dc.TenNguoiNhan, dc.SoDienThoai, " +
                "CONCAT(dc.DiaChiChiTiet, ', ', dc.PhuongXa, ', ', dc.QuanHuyen, ', ', dc.TinhThanh) AS DiaChiDayDu " +
                "FROM DonHang dh " +
                "LEFT JOIN DiaChi dc ON dh.MaDiaChi = dc.MaDiaChi " +
                "WHERE dh.MaDonHang = ? AND dh.MaNguoiDung = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maDonHang);
            ps.setInt(2, maNguoiDung);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Thông tin đơn hàng
                    chiTiet.setMaDonHang(rs.getInt("MaDonHang"));
                    chiTiet.setNgayTao(rs.getTimestamp("NgayTao"));
                    chiTiet.setTrangThai(rs.getString("TrangThai"));
                    chiTiet.setTongTien(rs.getBigDecimal("TongTien"));
                    chiTiet.setGiamGia(rs.getBigDecimal("GiamGia"));
                    chiTiet.setTienPhaiTra(rs.getBigDecimal("TienPhaiTra"));

                    // Thông tin người nhận
                    chiTiet.setTenNguoiNhan(rs.getString("TenNguoiNhan"));
                    chiTiet.setSoDienThoai(rs.getString("SoDienThoai"));
                    chiTiet.setDiaChiDayDu(rs.getString("DiaChiDayDu"));

                    // Lấy danh sách sản phẩm
                    List<ChiTietDonHang> danhSachSP = layChiTietDonHang(maDonHang);
                    chiTiet.setDanhSachSanPham(danhSachSP);
                } else {
                    throw new RuntimeException("Không tìm thấy đơn hàng hoặc bạn không có quyền truy cập");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy chi tiết đơn hàng: " + e.getMessage(), e);
        }

        return chiTiet;
    }


    /* API TẠO ĐƠN HÀNG

    /*
     * Tạo đơn hàng mới
     * @param donHang Thông tin đơn hàng
     * @return true nếu thành công, false nếu thất bại
     * Thêm chi tiết sản phẩm vào đơn hàng
     * @param maDonHang ID đơn hàng
     * @param chiTiet Chi tiết sản phẩm
     * @return true nếu thành công
     */

    // Cho phép hoạt động theo mô hình transactional
    /*
    Khi Spring thấy @Transactional, nó sẽ tạo proxy bao quanh method.
    Khi method được gọi:
        Spring mở transaction (bắt đầu).
        Thực thi code bên trong.
        Nếu mọi thứ thành công, nó commit (ghi vào DB).
        Nếu có exception, nó rollback (hủy toàn bộ thay đổi).
    *?
     */
    @Transactional
    public boolean taoDonHangVaChiTiet(DonHang donHang) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Tạo đơn hàng
            String sql = "INSERT INTO DonHang (MaNguoiDung, MaDiaChi, MaGiamGia, TongTien, GiamGia, TienPhaiTra, TrangThai) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 'Đang xử lý')";

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, donHang.getMaNguoiDung());
                ps.setObject(2, donHang.getMaDiaChi());
                ps.setObject(3, donHang.getMaGiamGia());
                ps.setBigDecimal(4, donHang.getTongTien());
                ps.setBigDecimal(5, donHang.getGiamGia() != null ? donHang.getGiamGia() : BigDecimal.ZERO);
                ps.setBigDecimal(6, donHang.getTienPhaiTra());

                int result = ps.executeUpdate();

                if (result > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            donHang.setMaDonHang(rs.getInt(1));
                        }
                    }
                }
            }

            // 2. Thêm chi tiết đơn hàng
            if (donHang.getChiTietDonHangList() != null && !donHang.getChiTietDonHangList().isEmpty()) {
                String sqlChiTiet = "INSERT INTO ChiTietDonHang (MaDonHang, MaSanPham, SoLuong, DonGia) " +
                        "VALUES (?, ?, ?, ?)";

                try (PreparedStatement psChiTiet = conn.prepareStatement(sqlChiTiet)) {
                    for (ChiTietDonHang chiTiet : donHang.getChiTietDonHangList()) {
                        psChiTiet.setInt(1, donHang.getMaDonHang());
                        psChiTiet.setInt(2, chiTiet.getMaSanPham());
                        psChiTiet.setInt(3, chiTiet.getSoLuong());
                        psChiTiet.setBigDecimal(4, chiTiet.getDonGia());
                        psChiTiet.addBatch();
                    }
                    psChiTiet.executeBatch();
                }
            }

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback nếu có lỗi
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Lỗi khi tạo đơn hàng: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Hủy đơn hàng
     * @param maDonHang ID đơn hàng
     * @param maNguoiDung ID người dùng (để verify quyền)
     * @return true nếu thành công
     */
    public boolean huyDonHang(int maDonHang, int maNguoiDung) {
        String sql = "UPDATE DonHang SET TrangThai = 'Đã hủy' " +
                "WHERE MaDonHang = ? AND MaNguoiDung = ? AND TrangThai = 'Đang xử lý'";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maDonHang);
            ps.setInt(2, maNguoiDung);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi hủy đơn hàng: " + e.getMessage(), e);
        }
    }
}