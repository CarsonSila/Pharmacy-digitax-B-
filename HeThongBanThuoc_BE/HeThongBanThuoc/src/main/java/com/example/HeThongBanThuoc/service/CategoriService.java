package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.model.DanhMuc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoriService {
    @Autowired
    private DataSource dataSource;

    // Lấy tất cả danh mục
    public List<DanhMuc> getAllDanhMuc() {
        List<DanhMuc> list = new ArrayList<>();
        String sql = "SELECT MaDanhMuc, TenDanhMuc, Slug, Icon, MaDanhMucCha FROM DanhMucThuoc";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                DanhMuc dm = new DanhMuc();
                dm.setMaDanhMuc(rs.getInt("MaDanhMuc"));
                dm.setTenDanhMuc(rs.getString("TenDanhMuc"));
                dm.setSlug(rs.getString("Slug"));
                dm.setIcon(rs.getString("Icon"));
                dm.setMaDanhMucCha(rs.getObject("MaDanhMucCha") != null ? rs.getInt("MaDanhMucCha") : null);
                list.add(dm);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh mục: " + e.getMessage(), e);
        }
        return list;
    }

    // Lấy danh mục CON nổi bật (có thể tùy chỉnh ngưỡng tối thiểu)
    public List<DanhMuc> getDanhMucNoiBat(int limit, int minSanPham) {
        List<DanhMuc> list = new ArrayList<>();
        String sql = """
        SELECT dm.MaDanhMuc, dm.TenDanhMuc, dm.Slug, dm.Icon, dm.MaDanhMucCha,
               COUNT(spdm.MaSanPham) AS SoLuongSanPham
        FROM DanhMucThuoc dm
        LEFT JOIN Sanpham_Danhmuc spdm ON dm.MaDanhMuc = spdm.MaDanhMuc
        WHERE dm.MaDanhMucCha IS NOT NULL
        GROUP BY dm.MaDanhMuc, dm.TenDanhMuc, dm.Slug, dm.Icon, dm.MaDanhMucCha
        HAVING COUNT(spdm.MaSanPham) >= ?
        ORDER BY SoLuongSanPham DESC
        LIMIT ?
    """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, minSanPham);  // Ngưỡng tối thiểu sản phẩm
            ps.setInt(2, limit);        // Số lượng kết quả tối đa

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DanhMuc dm = new DanhMuc();
                    dm.setMaDanhMuc(rs.getInt("MaDanhMuc"));
                    dm.setTenDanhMuc(rs.getString("TenDanhMuc"));
                    dm.setSlug(rs.getString("Slug"));
                    dm.setIcon(rs.getString("Icon"));
                    dm.setMaDanhMucCha(rs.getObject("MaDanhMucCha") != null ? rs.getInt("MaDanhMucCha") : null);
                    dm.setSoLuongSanPham(rs.getInt("SoLuongSanPham"));
                    list.add(dm);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh mục nổi bật: " + e.getMessage(), e);
        }
        return list;
    }

    /*
     * Lấy danh mục con theo danh mục cha
     * @param maDanhMucCha - ID danh mục cha
     * @return List danh mục con
     */
    public List<DanhMuc> getDanhMucConByMaCha(Integer maDanhMucCha) {
        List<DanhMuc> list = new ArrayList<>();
        String sql = """
            SELECT dm.MaDanhMuc, dm.TenDanhMuc, dm.Slug, dm.Icon, dm.MaDanhMucCha,
                   COUNT(spdm.MaSanPham) AS SoLuongSanPham
            FROM DanhMucThuoc dm
            LEFT JOIN Sanpham_Danhmuc spdm ON dm.MaDanhMuc = spdm.MaDanhMuc
            WHERE dm.MaDanhMucCha = ?
            GROUP BY dm.MaDanhMuc, dm.TenDanhMuc, dm.Slug, dm.Icon, dm.MaDanhMucCha
            ORDER BY dm.TenDanhMuc
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maDanhMucCha);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DanhMuc dm = new DanhMuc();
                    dm.setMaDanhMuc(rs.getInt("MaDanhMuc"));
                    dm.setTenDanhMuc(rs.getString("TenDanhMuc"));
                    dm.setSlug(rs.getString("Slug"));
                    dm.setIcon(rs.getString("Icon"));
                    dm.setMaDanhMucCha(rs.getObject("MaDanhMucCha") != null ? rs.getInt("MaDanhMucCha") : null);
                    dm.setSoLuongSanPham(rs.getInt("SoLuongSanPham"));
                    list.add(dm);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh mục con: " + e.getMessage(), e);
        }
        return list;
    }

    /*
     * Lấy chi tiết 1 danh mục theo ID
     * @param maDanhMuc - ID danh mục
     * @return DanhMuc hoặc null
     */
    public DanhMuc getDanhMucById(Integer maDanhMuc) {
        String sql = """
            SELECT dm.MaDanhMuc, dm.TenDanhMuc, dm.Slug, dm.Icon, dm.MaDanhMucCha,
                   COUNT(spdm.MaSanPham) AS SoLuongSanPham
            FROM DanhMucThuoc dm
            LEFT JOIN Sanpham_Danhmuc spdm ON dm.MaDanhMuc = spdm.MaDanhMuc
            WHERE dm.MaDanhMuc = ?
            GROUP BY dm.MaDanhMuc, dm.TenDanhMuc, dm.Slug, dm.Icon, dm.MaDanhMucCha
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maDanhMuc);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DanhMuc dm = new DanhMuc();
                    dm.setMaDanhMuc(rs.getInt("MaDanhMuc"));
                    dm.setTenDanhMuc(rs.getString("TenDanhMuc"));
                    dm.setSlug(rs.getString("Slug"));
                    dm.setIcon(rs.getString("Icon"));
                    dm.setMaDanhMucCha(rs.getObject("MaDanhMucCha") != null ? rs.getInt("MaDanhMucCha") : null);
                    dm.setSoLuongSanPham(rs.getInt("SoLuongSanPham"));
                    return dm;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy chi tiết danh mục: " + e.getMessage(), e);
        }
        return null;
    }
}
