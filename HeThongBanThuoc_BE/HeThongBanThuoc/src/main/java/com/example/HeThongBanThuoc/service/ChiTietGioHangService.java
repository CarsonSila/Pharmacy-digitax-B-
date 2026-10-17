package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.model.ChiTietGioHang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChiTietGioHangService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //  Lấy tất cả chi tiết giỏ hàng
    public List<ChiTietGioHang> getAll() {
        List<ChiTietGioHang> list = new ArrayList<>();
        String sql = """
        SELECT ct.*, 
               sp.TenThuoc AS TenSanPham,
               dv.TenDonVi AS DonViTinh,     
               (
                   SELECT asp.DuongDan
                   FROM AnhSanPham asp
                   WHERE asp.MaSanPham = sp.MaSanPham AND asp.AnhChinh = 1
                   ORDER BY asp.ThuTu ASC
                   LIMIT 1
               ) AS HinhAnh
        FROM ChiTietGioHang ct
        JOIN san_pham sp ON ct.MaSanPham = sp.MaSanPham
        JOIN DonViTinh dv ON sp.MaDonVi = dv.MaDonVi         
        ORDER BY ct.MaCTGH DESC
        """;
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy chi tiết giỏ hàng: " + e.getMessage(), e);
        }
        return list;
    }

    // Lấy chi tiết giỏ hàng theo mã giỏ hàng
    public List<ChiTietGioHang> getByGioHang(int maGioHang) {
        List<ChiTietGioHang> list = new ArrayList<>();
        String sql = """
        SELECT ct.*, 
               sp.TenThuoc AS TenSanPham,
               dv.TenDonVi AS DonViTinh,       
               (
                   SELECT asp.DuongDan
                   FROM AnhSanPham asp
                   WHERE asp.MaSanPham = sp.MaSanPham AND asp.AnhChinh = 1
                   ORDER BY asp.ThuTu ASC
                   LIMIT 1
               ) AS HinhAnh
        FROM ChiTietGioHang ct
        JOIN san_pham sp ON ct.MaSanPham = sp.MaSanPham
        JOIN DonViTinh dv ON sp.MaDonVi = dv.MaDonVi          
        WHERE ct.MaGioHang = ?
        """;
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maGioHang);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ChiTietGioHang ct = mapResultSet(rs);

                list.add(ct);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy chi tiết giỏ hàng theo mã: " + e.getMessage(), e);
        }
        return list;
    }

    // Thêm hoặc cập nhật sản phẩm trong giỏ
    public int create(ChiTietGioHang dto) {
        String checkSql = "SELECT MaCTGH, SoLuong FROM ChiTietGioHang WHERE MaGioHang = ? AND MaSanPham = ?";
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {

            ps.setInt(1, dto.getMaGioHang());
            ps.setInt(2, dto.getMaSanPham());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int maCTGH = rs.getInt("MaCTGH");
                int soLuongCu = rs.getInt("SoLuong");
                int soLuongMoi = soLuongCu + dto.getSoLuong();
                double donGiaMoi = dto.getDonGia();
                String updateSql = "UPDATE ChiTietGioHang SET SoLuong = ?, DonGia = ? WHERE MaCTGH = ?";
                return jdbcTemplate.update(updateSql, soLuongMoi, donGiaMoi, maCTGH);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi kiểm tra sản phẩm trong giỏ hàng: " + e.getMessage(), e);
        }

        String insertSql = "INSERT INTO ChiTietGioHang (MaGioHang, MaSanPham, SoLuong, DonGia) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(insertSql,
                dto.getMaGioHang(),
                dto.getMaSanPham(),
                dto.getSoLuong(),
                dto.getDonGia());
    }

    // Xóa chi tiết giỏ hàng
    public int delete(Integer id) {
        String sql = "DELETE FROM ChiTietGioHang WHERE MaCTGH = ?";
        return jdbcTemplate.update(sql, id);
    }

    // Helper ánh xạ dữ liệu
    private ChiTietGioHang mapResultSet(ResultSet rs) throws SQLException {
        ChiTietGioHang ct = new ChiTietGioHang();
        ct.setMaCTGH(rs.getInt("MaCTGH"));
        ct.setMaGioHang(rs.getInt("MaGioHang"));
        ct.setMaSanPham(rs.getInt("MaSanPham"));
        ct.setTenSanPham(rs.getString("TenSanPham"));
        ct.setDonViTinh(rs.getString("DonViTinh"));
        ct.setSoLuong(rs.getInt("SoLuong"));
        ct.setDonGia(rs.getDouble("DonGia"));
        ct.setHinhAnh(rs.getString("HinhAnh"));
        Timestamp ts = rs.getTimestamp("NgayTao");
        if (ts != null) ct.setNgayTao(ts.toLocalDateTime());
        return ct;
    }
}
