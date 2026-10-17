package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.model.DiaChi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class AddressService {

    @Autowired
    private DataSource dataSource;

    // Thêm địa chỉ mới
    public int themDiaChi(DiaChi dc) {
        String sql = "INSERT INTO DiaChi (MaNguoiDung, LoaiDiaChi, TenNguoiNhan, SoDienThoai, " +
                "DiaChiChiTiet, TinhThanh, QuanHuyen, PhuongXa, DiaChiMacDinh) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, dc.getMaNguoiDung());
            ps.setString(2, dc.getLoaiDiaChi());
            ps.setString(3, dc.getTenNguoiNhan());
            ps.setString(4, dc.getSoDienThoai());
            ps.setString(5, dc.getDiaChiChiTiet());
            ps.setString(6, dc.getTinhThanh());
            ps.setString(7, dc.getQuanHuyen());
            ps.setString(8, dc.getPhuongXa());
            ps.setBoolean(9, dc.getDiaChiMacDinh() != null && dc.getDiaChiMacDinh());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm địa chỉ: " + e.getMessage(), e);
        }
        return -1;
    }

    // Lấy danh sách địa chỉ của người dùng
    public List<DiaChi> layDanhSachDiaChiTheoNguoiDung(int maNguoiDung) {
        List<DiaChi> list = new ArrayList<>();
        String sql = "SELECT * FROM DiaChi WHERE MaNguoiDung = ? ORDER BY DiaChiMacDinh DESC, NgayTao DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maNguoiDung);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DiaChi dc = new DiaChi();
                    dc.setMaDiaChi(rs.getInt("MaDiaChi"));
                    dc.setMaNguoiDung(rs.getInt("MaNguoiDung"));
                    dc.setLoaiDiaChi(rs.getString("LoaiDiaChi"));
                    dc.setTenNguoiNhan(rs.getString("TenNguoiNhan"));
                    dc.setSoDienThoai(rs.getString("SoDienThoai"));
                    dc.setDiaChiChiTiet(rs.getString("DiaChiChiTiet"));
                    dc.setTinhThanh(rs.getString("TinhThanh"));
                    dc.setQuanHuyen(rs.getString("QuanHuyen"));
                    dc.setPhuongXa(rs.getString("PhuongXa"));
                    dc.setDiaChiMacDinh(rs.getBoolean("DiaChiMacDinh"));
                    dc.setNgayTao(rs.getTimestamp("NgayTao"));
                    list.add(dc);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy danh sách địa chỉ: " + e.getMessage(), e);
        }

        return list;
    }

    // Xóa địa chỉ
    public void xoaDiaChi(int maDiaChi, int maNguoiDung) {
        String sql = "DELETE FROM DiaChi WHERE MaDiaChi = ? AND MaNguoiDung = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maDiaChi);
            ps.setInt(2, maNguoiDung);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa địa chỉ: " + e.getMessage(), e);
        }
    }

    //  Đặt địa chỉ mặc định
    public void datDiaChiMacDinh(int maNguoiDung, int maDiaChi) {
        String sql1 = "UPDATE DiaChi SET DiaChiMacDinh = 0 WHERE MaNguoiDung = ?";
        String sql2 = "UPDATE DiaChi SET DiaChiMacDinh = 1 WHERE MaNguoiDung = ? AND MaDiaChi = ?";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sql1);
                 PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps1.setInt(1, maNguoiDung);
                ps1.executeUpdate();

                ps2.setInt(1, maNguoiDung);
                ps2.setInt(2, maDiaChi);
                ps2.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Lỗi khi cập nhật địa chỉ mặc định: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi đặt địa chỉ mặc định: " + e.getMessage(), e);
        }
    }

    // Cập nhật địa chỉ
    public void capNhatDiaChi(DiaChi dc) {
        String sql = "UPDATE DiaChi SET LoaiDiaChi = ?, TenNguoiNhan = ?, SoDienThoai = ?, " +
                "DiaChiChiTiet = ?, TinhThanh = ?, QuanHuyen = ?, PhuongXa = ?, DiaChiMacDinh = ? " +
                "WHERE MaDiaChi = ? AND MaNguoiDung = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dc.getLoaiDiaChi());
            ps.setString(2, dc.getTenNguoiNhan());
            ps.setString(3, dc.getSoDienThoai());
            ps.setString(4, dc.getDiaChiChiTiet());
            ps.setString(5, dc.getTinhThanh());
            ps.setString(6, dc.getQuanHuyen());
            ps.setString(7, dc.getPhuongXa());
            ps.setBoolean(8, dc.getDiaChiMacDinh() != null && dc.getDiaChiMacDinh());
            ps.setInt(9, dc.getMaDiaChi());
            ps.setInt(10, dc.getMaNguoiDung());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Không tìm thấy địa chỉ để cập nhật");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi cập nhật địa chỉ: " + e.getMessage(), e);
        }
    }

}

