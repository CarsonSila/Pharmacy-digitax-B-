package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.model.LichSuTimKiem;
import com.example.HeThongBanThuoc.model.SanPham;
import com.example.HeThongBanThuoc.model.TuKhoaTimKiem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private DataSource dataSource;

    /*
     * Tìm kiếm sản phẩm theo từ khóa
     */
    public List<SanPham> searchSanPham(String keyword, int page, int size, String sortBy, String sortDir, Integer userId) {
        List<SanPham> result = new ArrayList<>();

        // Kiểm tra cột được phép sort
        List<String> allowedSort = List.of("Gia", "TenThuoc");
        if (!allowedSort.contains(sortBy)) {
            sortBy = "Gia";
        }
        if (!sortDir.equalsIgnoreCase("ASC") && !sortDir.equalsIgnoreCase("DESC")) {
            sortDir = "ASC";
        }

        String sql = """
            SELECT sp.MaSanPham, sp.MaThuoc, sp.TenThuoc, sp.MoTaNgan, sp.Gia, 
                   sp.NhaSanXuat, sp.TrangThaiBan, asp.DuongDan,
                   dv.TenDonVi, dv.MoTa AS moTaDonVi
            FROM san_pham sp
            LEFT JOIN AnhSanPham asp ON sp.MaSanPham = asp.MaSanPham AND asp.AnhChinh = 1
            LEFT JOIN DonViTinh dv ON sp.MaDonVi = dv.MaDonVi
            WHERE (LOWER(sp.TenThuoc) LIKE LOWER(?) 
                   OR LOWER(sp.MoTaNgan) LIKE LOWER(?)
                   OR LOWER(sp.NhaSanXuat) LIKE LOWER(?))
              AND sp.TrangThaiBan = 1
            ORDER BY sp.""" + sortBy + " " + sortDir + " LIMIT ? OFFSET ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String likeKeyword = "%" + keyword + "%";
            ps.setString(1, likeKeyword);
            ps.setString(2, likeKeyword);
            ps.setString(3, likeKeyword);
            ps.setInt(4, size);
            ps.setInt(5, (page - 1) * size);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SanPham sp = new SanPham();
                    sp.setMaSanPham(rs.getInt("MaSanPham"));
                    sp.setMaThuoc(rs.getString("MaThuoc"));
                    sp.setTenThuoc(rs.getString("TenThuoc"));
                    sp.setMoTaNgan(rs.getString("MoTaNgan"));
                    sp.setGia(rs.getDouble("Gia"));
                    sp.setNhaSanXuat(rs.getString("NhaSanXuat"));
                    sp.setTrangThaiBan(rs.getInt("TrangThaiBan"));
                    sp.setDuongDanAnh(rs.getString("DuongDan"));
                    sp.setTenDonVi(rs.getString("TenDonVi"));
                    sp.setMoTaDonVi(rs.getString("MoTaDonVi"));
                    result.add(sp);
                }
            }

            // Lưu từ khóa tìm kiếm vào DB
            if (!keyword.isBlank()) {
                luuTuKhoaTimKiem(keyword);
                if (userId != null) {
                    luuLichSuTimKiem(userId, keyword);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm sản phẩm: " + e.getMessage(), e);
        }

        return result;
    }

    /*
     * Lưu/Cập nhật từ khóa vào bảng tu_khoa_tim_kiem
     */
    private void luuTuKhoaTimKiem(String keyword) {
        String sql = """
            INSERT INTO tu_khoa_tim_kiem (TuKhoa, SoLanTimKiem, LanTimKiemCuoi) 
            VALUES (?, 1, NOW())
            ON DUPLICATE KEY UPDATE 
                SoLanTimKiem = SoLanTimKiem + 1,
                LanTimKiemCuoi = NOW()
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, keyword.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi lưu từ khóa: " + e.getMessage());
        }
    }

    /*
     * Lưu lịch sử tìm kiếm của user
     */
    private void luuLichSuTimKiem(Integer maNguoiDung, String keyword) {
        String sql = "INSERT INTO lich_su_tim_kiem (MaNguoiDung, TuKhoa) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            ps.setString(2, keyword.trim());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi lưu lịch sử: " + e.getMessage());
        }
    }

    /*
     * Lấy top từ khóa phổ biến
     */
    public List<TuKhoaTimKiem> getTrendingKeywords(int limit) {
        List<TuKhoaTimKiem> result = new ArrayList<>();
        String sql = """
            SELECT TuKhoa, SoLanTimKiem, LanTimKiemCuoi 
            FROM tu_khoa_tim_kiem 
            ORDER BY SoLanTimKiem DESC 
            LIMIT ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TuKhoaTimKiem tk = new TuKhoaTimKiem();
                    tk.setTuKhoa(rs.getString("TuKhoa"));
                    tk.setSoLanTimKiem(rs.getInt("SoLanTimKiem"));
                    tk.setLanTimKiemCuoi(rs.getTimestamp("LanTimKiemCuoi").toLocalDateTime());
                    result.add(tk);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy trending keywords: " + e.getMessage(), e);
        }

        return result;
    }

    /*
     * Lấy lịch sử tìm kiếm của user
     */
    public List<LichSuTimKiem> getLichSuTimKiem(Integer maNguoiDung, int limit) {
        List<LichSuTimKiem> result = new ArrayList<>();
        String sql = """
            SELECT DISTINCT TuKhoa, MAX(ThoiGianTimKiem) as ThoiGianTimKiem, MAX(MaLichSu) as MaLichSu
            FROM lich_su_tim_kiem 
            WHERE MaNguoiDung = ? 
            GROUP BY TuKhoa
            ORDER BY ThoiGianTimKiem DESC 
            LIMIT ?
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LichSuTimKiem ls = new LichSuTimKiem();
                    ls.setMaLichSu(rs.getInt("MaLichSu"));
                    ls.setMaNguoiDung(maNguoiDung);
                    ls.setTuKhoa(rs.getString("TuKhoa"));
                    ls.setThoiGianTimKiem(rs.getTimestamp("ThoiGianTimKiem").toLocalDateTime());
                    result.add(ls);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy lịch sử: " + e.getMessage(), e);
        }

        return result;
    }

    /*
     * Xóa 1 item lịch sử
     */
    public boolean xoaLichSu(Integer maLichSu, Integer maNguoiDung) {
        String sql = "DELETE FROM lich_su_tim_kiem WHERE MaLichSu = ? AND MaNguoiDung = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maLichSu);
            ps.setInt(2, maNguoiDung);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa lịch sử: " + e.getMessage(), e);
        }
    }

    /*
     * Xóa tất cả lịch sử của user
     */
    public boolean xoaTatCaLichSu(Integer maNguoiDung) {
        String sql = "DELETE FROM lich_su_tim_kiem WHERE MaNguoiDung = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa tất cả lịch sử: " + e.getMessage(), e);
        }
    }

    /*
     * Lấy sản phẩm thay thế khi sản phẩm hết hàng (TrangThaiBan = 0)
     */
    public List<SanPham> getSanPhamThayThe(Integer maSanPham) {
        List<SanPham> result = new ArrayList<>();
        String sql = """
            SELECT sp.MaSanPham, sp.TenThuoc, sp.Gia, sp.NhaSanXuat, 
                   asp.DuongDan, dv.TenDonVi, dv.MoTa AS moTaDonVi
            FROM SanPhamLienQuan splq
            JOIN san_pham sp ON splq.MaSanPhamLQ = sp.MaSanPham
            LEFT JOIN AnhSanPham asp ON sp.MaSanPham = asp.MaSanPham AND asp.AnhChinh = 1
            LEFT JOIN DonViTinh dv ON sp.MaDonVi = dv.MaDonVi
            WHERE splq.MaSanPham = ? 
              AND splq.LoaiQuanHe = 'Thay thế'
              AND sp.TrangThaiBan = 1
            LIMIT 5
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maSanPham);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SanPham sp = new SanPham();
                    sp.setMaSanPham(rs.getInt("MaSanPham"));
                    sp.setTenThuoc(rs.getString("TenThuoc"));
                    sp.setGia(rs.getDouble("Gia"));
                    sp.setNhaSanXuat(rs.getString("NhaSanXuat"));
                    sp.setDuongDanAnh(rs.getString("DuongDan"));
                    sp.setTenDonVi(rs.getString("TenDonVi"));
                    sp.setMoTaDonVi(rs.getString("MoTaDonVi"));
                    result.add(sp);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy sản phẩm thay thế: " + e.getMessage(), e);
        }

        return result;
    }
}