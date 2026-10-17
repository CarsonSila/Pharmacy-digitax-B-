package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.model.GioHang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class GioHangService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Lấy tất cả giỏ hàng
    public List<GioHang> getAll() {
        List<GioHang> list = new ArrayList<>();
        String sql = """
            SELECT gh.*, nd.HoTen AS TenNguoiDung
            FROM GioHang gh
            JOIN NguoiDung nd ON gh.MaNguoiDung = nd.MaNguoiDung
            ORDER BY gh.MaGioHang DESC
        """;
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách giỏ hàng: " + e.getMessage(), e);
        }
        return list;
    }

    // Lấy danh sách giỏ hàng theo người dùng
    public List<GioHang> getByNguoiDung(int maNguoiDung) {
        List<GioHang> list = new ArrayList<>();
        String sql = """
            SELECT gh.*, nd.HoTen AS TenNguoiDung
            FROM GioHang gh 
            JOIN NguoiDung nd ON gh.MaNguoiDung = nd.MaNguoiDung
            WHERE gh.MaNguoiDung = ?
            ORDER BY gh.MaGioHang DESC
        """;
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy giỏ hàng người dùng: " + e.getMessage(), e);
        }
        return list;
    }

    // Xóa giỏ hàng
    public int delete(Integer id) {
        String sql = "DELETE FROM GioHang WHERE MaGioHang = ?";
        return jdbcTemplate.update(sql, id);
    }

    // Tìm giỏ hàng đang mở của người dùng
    public GioHang findActiveCartByUser(int maNguoiDung) {
        String sql = """
            SELECT * FROM GioHang
            WHERE MaNguoiDung = ? AND TrangThai = 'Đang mở'
            ORDER BY MaGioHang DESC LIMIT 1
        """;
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm giỏ hàng đang hoạt động: " + e.getMessage(), e);
        }
        return null;
    }

    // Nếu chưa có thì tạo giỏ mới
    public GioHang getOrCreateActiveCart(int maNguoiDung) {
        GioHang existing = findActiveCartByUser(maNguoiDung);
        if (existing != null) {
            return existing;
        }

        String sql = "INSERT INTO GioHang (MaNguoiDung, TrangThai, NgayTao, NgayCapNhat) VALUES (?, 'Đang mở', NOW(), NOW())";
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, maNguoiDung);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int newId = rs.getInt(1);
                return findById(newId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tạo giỏ hàng mới: " + e.getMessage(), e);
        }

        return null;
    }

    // Lấy giỏ hàng theo ID
    public GioHang findById(int maGioHang) {
        String sql = "SELECT * FROM GioHang WHERE MaGioHang = ?";
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maGioHang);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy giỏ hàng theo ID: " + e.getMessage(), e);
        }
        return null;
    }

    // Helper ánh xạ kết quả
    private GioHang mapResultSet(ResultSet rs) throws SQLException {
        GioHang gh = new GioHang();
        gh.setMaGioHang(rs.getInt("MaGioHang"));
        gh.setMaNguoiDung(rs.getInt("MaNguoiDung"));
        gh.setTrangThai(rs.getString("TrangThai"));
        Timestamp ts = rs.getTimestamp("NgayTao");
        if (ts != null) gh.setNgayTao(ts.toLocalDateTime());
        Timestamp ts2 = rs.getTimestamp("NgayCapNhat");
        return gh;
    }
}
