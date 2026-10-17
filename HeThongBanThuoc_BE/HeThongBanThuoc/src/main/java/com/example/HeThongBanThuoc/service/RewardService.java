package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RewardService {

    @Autowired
    private DataSource dataSource;

    // 1. Lấy số điểm hiện tại của user
    public Map<String, Object> getDiemBalance(Integer maNguoiDung) throws SQLException {
        String sql = "SELECT " +
                "  u.DiemTichLuy, " +
                "  COALESCE(SUM(CASE WHEN d.NgayHetHan <= DATE_ADD(NOW(), INTERVAL 7 DAY) " +
                "                    AND d.TrangThai = 'HieuLuc' THEN d.SoDiem ELSE 0 END), 0) AS DiemSapHetHan, " +
                "  MIN(CASE WHEN d.TrangThai = 'HieuLuc' THEN d.NgayHetHan END) AS NgayHetHanGanNhat " +
                "FROM NguoiDung u " +
                "LEFT JOIN DiemThuong d ON u.MaNguoiDung = d.MaNguoiDung " +
                "WHERE u.MaNguoiDung = ? " +
                "GROUP BY u.MaNguoiDung, u.DiemTichLuy";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maNguoiDung);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Map<String, Object> result = new HashMap<>();
                result.put("maNguoiDung", maNguoiDung);
                result.put("tongDiem", rs.getInt("DiemTichLuy"));
                result.put("diemSapHetHan", rs.getInt("DiemSapHetHan"));

                Timestamp ngayHetHan = rs.getTimestamp("NgayHetHanGanNhat");
                result.put("ngayHetHanGanNhat", ngayHetHan != null ? ngayHetHan.toLocalDateTime() : null);

                return result;
            }
            throw new RuntimeException("Không tìm thấy người dùng");
        }
    }

    // 2. Lấy lịch sử tích/tiêu điểm
    public List<DiemThuong> getLichSuDiem(Integer maNguoiDung) throws SQLException {
        String sql = "SELECT * FROM DiemThuong " +
                "WHERE MaNguoiDung = ? " +
                "ORDER BY NgayTao DESC " +
                "LIMIT 50";

        List<DiemThuong> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maNguoiDung);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DiemThuong dt = new DiemThuong();
                dt.setMaDiem(rs.getInt("MaDiem"));
                dt.setMaNguoiDung(rs.getInt("MaNguoiDung"));
                dt.setMaDonHang(rs.getObject("MaDonHang", Integer.class));
                dt.setLoaiGiaoDich(rs.getString("LoaiGiaoDich"));
                dt.setSoDiem(rs.getInt("SoDiem"));
                dt.setSoDiemConLai(rs.getInt("SoDiemConLai"));
                dt.setMoTa(rs.getString("MoTa"));
                dt.setNgayTao(rs.getTimestamp("NgayTao").toLocalDateTime());
                dt.setNgayHetHan(rs.getTimestamp("NgayHetHan").toLocalDateTime());
                dt.setTrangThai(rs.getString("TrangThai"));
                list.add(dt);
            }
        }
        return list;
    }

    // 3. Lấy danh sách quà tặng (có filter)
    public List<QuaTang> getDanhSachQuaTang(Integer diemFilter) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM QuaTang WHERE TrangThai = 1");

        if (diemFilter != null && diemFilter > 0) {
            sql.append(" AND DiemCanDoi = ?");
        }

        sql.append(" ORDER BY DiemCanDoi ASC");

        List<QuaTang> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (diemFilter != null && diemFilter > 0) {
                ps.setInt(1, diemFilter);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                QuaTang qt = new QuaTang();
                qt.setMaQuaTang(rs.getInt("MaQuaTang"));
                qt.setTenQuaTang(rs.getString("TenQuaTang"));
                qt.setMoTa(rs.getString("MoTa"));
                qt.setHinhAnh(rs.getString("HinhAnh"));
                qt.setDiemCanDoi(rs.getInt("DiemCanDoi"));
                qt.setSoLuongTon(rs.getInt("SoLuongTon"));
                qt.setTrangThai(rs.getBoolean("TrangThai"));
                qt.setNgayTao(rs.getTimestamp("NgayTao").toLocalDateTime());
                qt.setNgayCapNhat(rs.getTimestamp("NgayCapNhat").toLocalDateTime());
                list.add(qt);
            }
        }
        return list;
    }

    // 4. Đổi quà - ĐÃ CẬP NHẬT: Thêm HinhAnh
    @Transactional
    public Map<String, Object> doiQua(Integer maNguoiDung, Integer maQuaTang,
                                      Integer soLuong, String ghiChu) throws SQLException {

        // Set default soLuong = 1 nếu null
        if (soLuong == null || soLuong <= 0) {
            soLuong = 1;
        }

        // Kiểm tra điểm
        Integer diemHienTai = getDiemHienTai(maNguoiDung);

        // Lấy thông tin quà
        QuaTang quaTang = getQuaTangById(maQuaTang);
        if (quaTang == null) {
            throw new RuntimeException("Quà tặng không tồn tại");
        }

        Integer tongDiemCanDoi = quaTang.getDiemCanDoi() * soLuong;

        if (diemHienTai < tongDiemCanDoi) {
            throw new RuntimeException("Không đủ điểm để đổi quà");
        }

        // Kiểm tra tồn kho
        if (quaTang.getSoLuongTon() > 0 && quaTang.getSoLuongTon() < soLuong) {
            throw new RuntimeException("Quà tặng không đủ số lượng");
        }

        // ← ĐÃ THÊM HinhAnh VÀO INSERT
        String sqlInsert = "INSERT INTO LichSuDoiQua (MaNguoiDung, MaQuaTang, DiemSuDung, " +
                "SoLuong, TenQuaTang, HinhAnh, DiemCanDoiLucDoi, GhiChu, TrangThai) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Chờ xử lý')";

        String sqlUpdateDiem = "UPDATE NguoiDung SET DiemTichLuy = DiemTichLuy - ? WHERE MaNguoiDung = ?";

        String sqlUpdateTon = "UPDATE QuaTang SET SoLuongTon = SoLuongTon - ? WHERE MaQuaTang = ?";

        String sqlInsertGiaoDich = "INSERT INTO DiemThuong (MaNguoiDung, LoaiGiaoDich, " +
                "SoDiem, SoDiemConLai, MoTa, NgayHetHan, TrangThai) " +
                "VALUES (?, 'Tieu', ?, ?, ?, ?, 'DaSuDung')";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            Integer maDoiQua = null;

            try {
                // 1. Insert lịch sử đổi quà
                try (PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, maNguoiDung);
                    ps.setInt(2, maQuaTang);
                    ps.setInt(3, tongDiemCanDoi);
                    ps.setInt(4, soLuong);
                    ps.setString(5, quaTang.getTenQuaTang());
                    ps.setString(6, quaTang.getHinhAnh()); // ← ĐÃ THÊM
                    ps.setInt(7, quaTang.getDiemCanDoi());
                    ps.setString(8, ghiChu);
                    ps.executeUpdate();

                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        maDoiQua = rs.getInt(1);
                    }
                }

                // 2. Trừ điểm user
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateDiem)) {
                    ps.setInt(1, tongDiemCanDoi);
                    ps.setInt(2, maNguoiDung);
                    ps.executeUpdate();
                }

                // 3. Trừ tồn kho (nếu có)
                if (quaTang.getSoLuongTon() > 0) {
                    try (PreparedStatement ps = conn.prepareStatement(sqlUpdateTon)) {
                        ps.setInt(1, soLuong);
                        ps.setInt(2, maQuaTang);
                        ps.executeUpdate();
                    }
                }

                // 4. Tạo giao dịch tiêu điểm
                try (PreparedStatement ps = conn.prepareStatement(sqlInsertGiaoDich)) {
                    ps.setInt(1, maNguoiDung);
                    ps.setInt(2, -tongDiemCanDoi);
                    ps.setInt(3, diemHienTai - tongDiemCanDoi);
                    ps.setString(4, "Đổi quà: " + quaTang.getTenQuaTang());
                    ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now().plusMonths(3)));
                    ps.executeUpdate();
                }

                conn.commit();

                // Trả về kết quả dạng Map
                Map<String, Object> result = new HashMap<>();
                result.put("maDoiQua", maDoiQua);
                result.put("maNguoiDung", maNguoiDung);
                result.put("maQuaTang", maQuaTang);
                result.put("diemSuDung", tongDiemCanDoi);
                result.put("soLuong", soLuong);
                result.put("tenQuaTang", quaTang.getTenQuaTang());
                result.put("hinhAnh", quaTang.getHinhAnh()); // ← ĐÃ THÊM
                result.put("trangThai", "Chờ xử lý");
                return result;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // 5. Lấy lịch sử đổi quà - ĐÃ CẬP NHẬT: JOIN để lấy HinhAnh
    public List<LichSuDoiQua> getLichSuDoiQua(Integer maNguoiDung) throws SQLException {
        // ← ĐÃ THÊM LEFT JOIN VÀ COALESCE
        String sql = "SELECT ls.*, " +
                "COALESCE(ls.HinhAnh, qt.HinhAnh) AS HinhAnh " +
                "FROM LichSuDoiQua ls " +
                "LEFT JOIN QuaTang qt ON ls.MaQuaTang = qt.MaQuaTang " +
                "WHERE ls.MaNguoiDung = ? " +
                "ORDER BY ls.NgayDoi DESC " +
                "LIMIT 50";

        List<LichSuDoiQua> list = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maNguoiDung);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LichSuDoiQua ls = new LichSuDoiQua();
                ls.setMaDoiQua(rs.getInt("MaDoiQua"));
                ls.setMaNguoiDung(rs.getInt("MaNguoiDung"));
                ls.setMaQuaTang(rs.getInt("MaQuaTang"));
                ls.setDiemSuDung(rs.getInt("DiemSuDung"));
                ls.setSoLuong(rs.getInt("SoLuong"));
                ls.setTenQuaTang(rs.getString("TenQuaTang"));
                ls.setHinhAnh(rs.getString("HinhAnh")); // ← ĐÃ THÊM
                ls.setDiemCanDoiLucDoi(rs.getInt("DiemCanDoiLucDoi"));
                ls.setGhiChu(rs.getString("GhiChu"));
                ls.setTrangThai(rs.getString("TrangThai"));
                ls.setLyDoTuChoi(rs.getString("LyDoTuChoi"));
                ls.setNgayDoi(rs.getTimestamp("NgayDoi").toLocalDateTime());

                Timestamp ngayXuLy = rs.getTimestamp("NgayXuLy");
                if (ngayXuLy != null) {
                    ls.setNgayXuLy(ngayXuLy.toLocalDateTime());
                }

                ls.setNguoiXuLy(rs.getObject("NguoiXuLy", Integer.class));
                list.add(ls);
            }
        }
        return list;
    }

    // Helper Methods
    private Integer getDiemHienTai(Integer maNguoiDung) throws SQLException {
        String sql = "SELECT DiemTichLuy FROM NguoiDung WHERE MaNguoiDung = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maNguoiDung);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("DiemTichLuy");
            }
            return 0;
        }
    }

    private QuaTang getQuaTangById(Integer maQuaTang) throws SQLException {
        String sql = "SELECT * FROM QuaTang WHERE MaQuaTang = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maQuaTang);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                QuaTang qt = new QuaTang();
                qt.setMaQuaTang(rs.getInt("MaQuaTang"));
                qt.setTenQuaTang(rs.getString("TenQuaTang"));
                qt.setMoTa(rs.getString("MoTa"));
                qt.setHinhAnh(rs.getString("HinhAnh"));
                qt.setDiemCanDoi(rs.getInt("DiemCanDoi"));
                qt.setSoLuongTon(rs.getInt("SoLuongTon"));
                qt.setTrangThai(rs.getBoolean("TrangThai"));
                return qt;
            }
            return null;
        }
    }
}