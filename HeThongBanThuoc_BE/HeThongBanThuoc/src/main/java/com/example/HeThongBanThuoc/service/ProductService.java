package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.model.SanPham;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    // Lấy sản phẩm bán chạy
    public List<SanPham> getBestSellingProducts(int limit) {
        String sql = """
        SELECT sp.MaSanPham, sp.MaThuoc, sp.TenThuoc, sp.MoTaNgan,
               sp.Gia, sp.NhaSanXuat,
               (SELECT a.DuongDan
                FROM AnhSanPham a
                WHERE a.MaSanPham = sp.MaSanPham AND a.AnhChinh = 1
                LIMIT 1) AS DuongDanAnh,
               SUM(ct.SoLuong) AS TongSoLuong,
               GROUP_CONCAT(DISTINCT dm.TenDanhMuc SEPARATOR ', ') AS TenDanhMuc
        FROM ChiTietDonHang ct
        JOIN DonHang dh ON ct.MaDonHang = dh.MaDonHang
        JOIN san_pham sp ON ct.MaSanPham = sp.MaSanPham
        LEFT JOIN Sanpham_Danhmuc spdm ON sp.MaSanPham = spdm.MaSanPham
        LEFT JOIN DanhMucThuoc dm ON spdm.MaDanhMuc = dm.MaDanhMuc
        WHERE dh.TrangThai = 'Đã giao'
          AND dh.NgayTao >= DATE_SUB(NOW(), INTERVAL 30 DAY)
        GROUP BY sp.MaSanPham, sp.MaThuoc, sp.TenThuoc, sp.MoTaNgan,
                 sp.Gia, sp.NhaSanXuat
        HAVING TongSoLuong >= 5
        ORDER BY TongSoLuong DESC
        LIMIT ?
    """;

        List<SanPham> result;
        try {
            result = jdbcTemplate.query(sql, new Object[]{limit}, (rs, rowNum) -> {
                SanPham sp = new SanPham();
                sp.setMaSanPham(rs.getInt("MaSanPham"));
                sp.setMaThuoc(rs.getString("MaThuoc"));
                sp.setTenThuoc(rs.getString("TenThuoc"));
                sp.setMoTaNgan(rs.getString("MoTaNgan"));
                sp.setGia(rs.getDouble("Gia"));
                sp.setNhaSanXuat(rs.getString("NhaSanXuat"));
                sp.setDuongDanAnh(rs.getString("DuongDanAnh"));
                sp.setTongBan(rs.getInt("TongSoLuong"));
                sp.setTenDanhMuc(rs.getString("TenDanhMuc"));
                return sp;
            });
        } catch (Exception e) {
            System.out.println("[ProductService] Lỗi khi lấy sản phẩm bán chạy: " + e.getMessage());
            result = new java.util.ArrayList<>();
        }

        // Nếu chưa đủ số lượng sản phẩm bán chạy, bổ sung thêm sản phẩm mới
        if (result.size() < limit) {
            int remaining = limit - result.size();
            List<SanPham> newProducts = getNewProducts(remaining, result);
            result.addAll(newProducts);
        }

        return result;
    }

    // PHƯƠNG THỨC HỖ TRỢ: Lấy sản phẩm mới nhất (fallback)
    private List<SanPham> getNewProducts(int limit, List<SanPham> excludeList) {
        // Tạo danh sách ID cần loại trừ
        String excludeIds = excludeList.stream()
                .map(sp -> String.valueOf(sp.getMaSanPham()))
                .collect(Collectors.joining(","));

        String whereClause = excludeIds.isEmpty() ? "" : "WHERE sp.MaSanPham NOT IN (" + excludeIds + ")";

        String sql = """
        SELECT sp.MaSanPham, sp.MaThuoc, sp.TenThuoc, sp.MoTaNgan,
               sp.Gia, sp.NhaSanXuat,
               (SELECT a.DuongDan
                FROM AnhSanPham a
                WHERE a.MaSanPham = sp.MaSanPham AND a.AnhChinh = 1
                LIMIT 1) AS DuongDanAnh,
               0 AS TongSoLuong,
               GROUP_CONCAT(DISTINCT dm.TenDanhMuc SEPARATOR ', ') AS TenDanhMuc
        FROM san_pham sp
        JOIN Sanpham_Danhmuc spdm ON sp.MaSanPham = spdm.MaSanPham
        JOIN DanhMucThuoc dm ON spdm.MaDanhMuc = dm.MaDanhMuc
        """ + whereClause + """
        GROUP BY sp.MaSanPham
        ORDER BY sp.NgayTao DESC
        LIMIT ?
    """;

        return jdbcTemplate.query(sql, new Object[]{limit}, (rs, rowNum) -> {
            SanPham sp = new SanPham();
            sp.setMaSanPham(rs.getInt("MaSanPham"));
            sp.setMaThuoc(rs.getString("MaThuoc"));
            sp.setTenThuoc(rs.getString("TenThuoc"));
            sp.setMoTaNgan(rs.getString("MoTaNgan"));
            sp.setGia(rs.getDouble("Gia"));
            sp.setNhaSanXuat(rs.getString("NhaSanXuat"));
            sp.setDuongDanAnh(rs.getString("DuongDanAnh"));
            sp.setTongBan(0);  // Sản phẩm mới chưa bán
            sp.setTenDanhMuc(rs.getString("TenDanhMuc"));
            return sp;
        });
    }

    // Lấy tất cả sản phẩm
    public List<SanPham> getAllProducts() {
        String sql = """
            SELECT sp.MaSanPham, sp.MaThuoc, sp.TenThuoc, sp.MoTaNgan,
                   sp.Gia, sp.NhaSanXuat,
                   (SELECT a.DuongDan
                    FROM AnhSanPham a
                    WHERE a.MaSanPham = sp.MaSanPham AND a.AnhChinh = 1
                    LIMIT 1) AS DuongDanAnh,
                   GROUP_CONCAT(dm.TenDanhMuc SEPARATOR ', ') AS TenDanhMuc
            FROM san_pham sp
            LEFT JOIN Sanpham_Danhmuc spdm ON sp.MaSanPham = spdm.MaSanPham
            LEFT JOIN DanhMucThuoc dm ON spdm.MaDanhMuc = dm.MaDanhMuc
            GROUP BY sp.MaSanPham
            ORDER BY sp.MaSanPham DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            SanPham sp = new SanPham();
            sp.setMaSanPham(rs.getInt("MaSanPham"));
            sp.setMaThuoc(rs.getString("MaThuoc"));
            sp.setTenThuoc(rs.getString("TenThuoc"));
            sp.setMoTaNgan(rs.getString("MoTaNgan"));
            sp.setGia(rs.getDouble("Gia"));
            sp.setNhaSanXuat(rs.getString("NhaSanXuat"));
            sp.setDuongDanAnh(rs.getString("DuongDanAnh"));
            sp.setTenDanhMuc(rs.getString("TenDanhMuc"));
            return sp;
        });
    }

    /*
     * Lấy sản phẩm theo danh mục (bao gồm cả danh mục con)
     * @param maDanhMuc - ID danh mục
     * @return List sản phẩm
     */
    // ProductService.java
    public List<SanPham> getSanPhamByDanhMuc(Integer maDanhMuc, String sort) {
        // Xác định ORDER BY clause dựa trên tham số sort
        String orderByClause;
        switch (sort.toLowerCase()) {
            case "gia-thap":
                orderByClause = "sp.Gia ASC";
                break;
            case "gia-cao":
                orderByClause = "sp.Gia DESC";
                break;
            case "ban-chay":
            default:
                orderByClause = "TongBan DESC, sp.MaSanPham DESC";
                break;
        }

        String sql = """
        SELECT DISTINCT
            sp.MaSanPham,
            sp.MaThuoc,
            sp.TenThuoc,
            sp.MoTaNgan,
            sp.Gia,
            sp.NhaSanXuat,
            sp.TrangThaiBan,
            dv.TenDonVi,
            dv.MoTa AS MoTaDonVi,
            COALESCE(tk.SoLuong, 0) AS SoLuongTonKho,
            (SELECT a.DuongDan
             FROM AnhSanPham a
             WHERE a.MaSanPham = sp.MaSanPham AND a.AnhChinh = 1
             LIMIT 1) AS DuongDanAnh,
            (SELECT COALESCE(SUM(ct.SoLuong), 0)
             FROM ChiTietDonHang ct
             JOIN DonHang dh ON ct.MaDonHang = dh.MaDonHang
             WHERE ct.MaSanPham = sp.MaSanPham 
               AND dh.TrangThai = 'Đã giao') AS TongBan
        FROM san_pham sp
        JOIN Sanpham_Danhmuc spdm ON sp.MaSanPham = spdm.MaSanPham
        JOIN DanhMucThuoc dm ON spdm.MaDanhMuc = dm.MaDanhMuc
        LEFT JOIN DonViTinh dv ON sp.MaDonVi = dv.MaDonVi
        LEFT JOIN TonKho tk ON sp.MaSanPham = tk.MaSanPham
        WHERE (dm.MaDanhMuc = ? OR dm.MaDanhMucCha = ?)
          AND sp.TrangThaiBan = 1
        """ + " ORDER BY " + orderByClause;


        return jdbcTemplate.query(sql, new Object[]{maDanhMuc, maDanhMuc}, (rs, rowNum) -> {
            SanPham sp = new SanPham();
            sp.setMaSanPham(rs.getInt("MaSanPham"));
            sp.setMaThuoc(rs.getString("MaThuoc"));
            sp.setTenThuoc(rs.getString("TenThuoc"));
            sp.setMoTaNgan(rs.getString("MoTaNgan"));
            sp.setGia(rs.getDouble("Gia"));
            sp.setNhaSanXuat(rs.getString("NhaSanXuat"));
            sp.setTrangThaiBan(rs.getInt("TrangThaiBan"));
            sp.setDuongDanAnh(rs.getString("DuongDanAnh"));
            sp.setTongBan(rs.getInt("TongBan"));
            sp.setTenDonVi(rs.getString("TenDonVi"));
            sp.setMoTaDonVi(rs.getString("MoTaDonVi"));
            sp.setSoLuongTonKho(rs.getInt("SoLuongTonKho"));
            return sp;
        });
    }

    /*
     * Lấy chi tiết 1 sản phẩm
     * @param maSanPham - ID sản phẩm
     * @return SanPham hoặc null
     */
    public SanPham getSanPhamById(Integer maSanPham) {
        String sql = """
            SELECT 
                sp.MaSanPham,
                sp.MaThuoc,
                sp.TenThuoc,
                sp.MoTaNgan,
                sp.MoTaChiTiet,
                sp.Gia,
                sp.NhaSanXuat,
                sp.TrangThaiBan,
                sp.HanSuDung,
                dv.TenDonVi,
                dv.MoTa AS MoTaDonVi,
                COALESCE(tk.SoLuong, 0) AS SoLuongTonKho,
                (SELECT a.DuongDan
                 FROM AnhSanPham a
                 WHERE a.MaSanPham = sp.MaSanPham AND a.AnhChinh = 1
                 LIMIT 1) AS DuongDanAnh,
                (SELECT COALESCE(SUM(ct.SoLuong), 0)
                 FROM ChiTietDonHang ct
                 JOIN DonHang dh ON ct.MaDonHang = dh.MaDonHang
                 WHERE ct.MaSanPham = sp.MaSanPham 
                   AND dh.TrangThai = 'Đã giao') AS TongBan,
                GROUP_CONCAT(DISTINCT dm.TenDanhMuc SEPARATOR ', ') AS TenDanhMuc
            FROM san_pham sp
            LEFT JOIN DonViTinh dv ON sp.MaDonVi = dv.MaDonVi
            LEFT JOIN TonKho tk ON sp.MaSanPham = tk.MaSanPham
            LEFT JOIN Sanpham_Danhmuc spdm ON sp.MaSanPham = spdm.MaSanPham
            LEFT JOIN DanhMucThuoc dm ON spdm.MaDanhMuc = dm.MaDanhMuc
            WHERE sp.MaSanPham = ?
            GROUP BY sp.MaSanPham
        """;

        List<SanPham> results = jdbcTemplate.query(sql, new Object[]{maSanPham}, (rs, rowNum) -> {
            SanPham sp = new SanPham();
            sp.setMaSanPham(rs.getInt("MaSanPham"));
            sp.setMaThuoc(rs.getString("MaThuoc"));
            sp.setTenThuoc(rs.getString("TenThuoc"));
            sp.setMoTaNgan(rs.getString("MoTaNgan"));

            sp.setGia(rs.getDouble("Gia"));

            sp.setNhaSanXuat(rs.getString("NhaSanXuat"));
            sp.setTrangThaiBan(rs.getInt("TrangThaiBan"));
            sp.setDuongDanAnh(rs.getString("DuongDanAnh"));
            sp.setTongBan(rs.getInt("TongBan"));
            sp.setTenDonVi(rs.getString("TenDonVi"));
            sp.setMoTaDonVi(rs.getString("MoTaDonVi"));
            sp.setTenDanhMuc(rs.getString("TenDanhMuc"));
            sp.setSoLuongTonKho(rs.getInt("SoLuongTonKho"));

            return sp;
        });

        return results.isEmpty() ? null : results.get(0);
    }
}
