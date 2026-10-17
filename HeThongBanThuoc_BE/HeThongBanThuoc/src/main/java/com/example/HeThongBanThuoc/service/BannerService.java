package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.model.Banner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class BannerService {
    @Autowired
    private DataSource dataSource;

    // Lấy tất cả banner đang hiển thị
    public List<Banner> getAllActiveBanners() {
        List<Banner> banners = new ArrayList<>();
        String sql = "SELECT * FROM Banner WHERE TrangThai = 'Hiển thị' ORDER BY NgayTao DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Banner b = new Banner();
                b.setMaBanner(rs.getInt("MaBanner"));
                b.setHinhAnh(rs.getString("HinhAnh"));
                b.setLienKet(rs.getString("LienKet"));
                b.setTrangThai(rs.getString("TrangThai"));
                b.setNgayTao(rs.getTimestamp("NgayTao").toLocalDateTime());
                banners.add(b);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách banner: " + e.getMessage(), e);
        }
        return banners;
    }
}
