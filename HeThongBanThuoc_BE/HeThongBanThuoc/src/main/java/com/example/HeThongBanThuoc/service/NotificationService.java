package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.model.ThongBao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper để map dữ liệu từ DB sang model
    private RowMapper<ThongBao> thongBaoMapper = new RowMapper<>() {
        @Override
        public ThongBao mapRow(ResultSet rs, int rowNum) throws SQLException {
            ThongBao tb = new ThongBao();
            tb.setMaThongBao(rs.getInt("MaThongBao"));
            tb.setMaNguoiDung(rs.getInt("MaNguoiDung"));
            tb.setTieuDe(rs.getString("TieuDe"));
            tb.setNoiDung(rs.getString("NoiDung"));
            tb.setLoaiThongBao(rs.getString("LoaiThongBao"));
            tb.setTrangThai(rs.getString("TrangThai"));
            tb.setNgayTao(rs.getTimestamp("NgayTao").toLocalDateTime());
            if (rs.getTimestamp("NgayDoc") != null) {
                tb.setNgayDoc(rs.getTimestamp("NgayDoc").toLocalDateTime());
            }
            return tb;
        }
    };

    // 1. Lấy danh sách thông báo có phân trang
    public List<ThongBao> getThongBaoByUser(int userId, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT * FROM ThongBao WHERE MaNguoiDung = ? " +
                "ORDER BY NgayTao DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, thongBaoMapper, userId, size, offset);
    }

    // 2. Đếm số thông báo chưa đọc
    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM ThongBao WHERE MaNguoiDung = ? AND TrangThai = 'Chưa đọc'";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId);
    }

    // 3. Đánh dấu 1 thông báo đã đọc
    public boolean markAsRead(int userId, int notificationId) {
        String sql = "UPDATE ThongBao SET TrangThai = 'Đã đọc', NgayDoc = NOW() " +
                "WHERE MaThongBao = ? AND MaNguoiDung = ?";
        return jdbcTemplate.update(sql, notificationId, userId) > 0;
    }

    // 4. Đánh dấu tất cả thông báo đã đọc
    public int markAllAsRead(int userId) {
        String sql = "UPDATE ThongBao SET TrangThai = 'Đã đọc', NgayDoc = NOW() " +
                "WHERE MaNguoiDung = ? AND TrangThai = 'Chưa đọc'";
        return jdbcTemplate.update(sql, userId);
    }

    // ==================== CÁC CHỨC NĂNG BỔ SUNG ====================

    // 5. Lấy thông báo theo loại (Đơn hàng, Hệ thống, Khuyến mãi, Đổi quà, Tư vấn)
    public List<ThongBao> getThongBaoByLoai(int userId, String loaiThongBao, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT * FROM ThongBao WHERE MaNguoiDung = ? AND LoaiThongBao = ? " +
                "ORDER BY NgayTao DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, thongBaoMapper, userId, loaiThongBao, size, offset);
    }

    // 6. Lấy chi tiết 1 thông báo
    public ThongBao getThongBaoById(int userId, int notificationId) {
        String sql = "SELECT * FROM ThongBao WHERE MaThongBao = ? AND MaNguoiDung = ?";
        List<ThongBao> result = jdbcTemplate.query(sql, thongBaoMapper, notificationId, userId);
        return result.isEmpty() ? null : result.get(0);
    }

    // 7. Tạo thông báo mới (Admin hoặc hệ thống tự động tạo)
    public boolean createThongBao(ThongBao thongBao) {
        String sql = "INSERT INTO ThongBao (MaNguoiDung, TieuDe, NoiDung, LoaiThongBao, TrangThai) " +
                "VALUES (?, ?, ?, ?, 'Chưa đọc')";
        return jdbcTemplate.update(sql,
                thongBao.getMaNguoiDung(),
                thongBao.getTieuDe(),
                thongBao.getNoiDung(),
                thongBao.getLoaiThongBao()) > 0;
    }

    // 8. Xóa 1 thông báo
    public boolean deleteThongBao(int userId, int notificationId) {
        String sql = "DELETE FROM ThongBao WHERE MaThongBao = ? AND MaNguoiDung = ?";
        return jdbcTemplate.update(sql, notificationId, userId) > 0;
    }

    // 9. Xóa tất cả thông báo đã đọc
    public int deleteAllRead(int userId) {
        String sql = "DELETE FROM ThongBao WHERE MaNguoiDung = ? AND TrangThai = 'Đã đọc'";
        return jdbcTemplate.update(sql, userId);
    }

    // 10. Đếm tổng số thông báo của user
    public int countTotalThongBao(int userId) {
        String sql = "SELECT COUNT(*) FROM ThongBao WHERE MaNguoiDung = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId);
    }

    // 11. Lấy thông báo chưa đọc (không phân trang)
    public List<ThongBao> getUnreadNotifications(int userId) {
        String sql = "SELECT * FROM ThongBao WHERE MaNguoiDung = ? AND TrangThai = 'Chưa đọc' " +
                "ORDER BY NgayTao DESC";
        return jdbcTemplate.query(sql, thongBaoMapper, userId);
    }

    // 12. Đếm số thông báo theo loại
    public int countByLoai(int userId, String loaiThongBao) {
        String sql = "SELECT COUNT(*) FROM ThongBao WHERE MaNguoiDung = ? AND LoaiThongBao = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, loaiThongBao);
    }

    // 13. Xóa tất cả thông báo của user
    public int deleteAllThongBao(int userId) {
        String sql = "DELETE FROM ThongBao WHERE MaNguoiDung = ?";
        return jdbcTemplate.update(sql, userId);
    }

    // 14. Lấy thông báo theo khoảng thời gian
    public List<ThongBao> getThongBaoByDateRange(int userId, String startDate, String endDate) {
        String sql = "SELECT * FROM ThongBao WHERE MaNguoiDung = ? " +
                "AND DATE(NgayTao) BETWEEN ? AND ? " +
                "ORDER BY NgayTao DESC";
        return jdbcTemplate.query(sql, thongBaoMapper, userId, startDate, endDate);
    }

    // 15. Tìm kiếm thông báo theo từ khóa (tiêu đề hoặc nội dung)
    public List<ThongBao> searchThongBao(int userId, String keyword, int page, int size) {
        int offset = (page - 1) * size;
        String sql = "SELECT * FROM ThongBao WHERE MaNguoiDung = ? " +
                "AND (TieuDe LIKE ? OR NoiDung LIKE ?) " +
                "ORDER BY NgayTao DESC LIMIT ? OFFSET ?";
        String searchPattern = "%" + keyword + "%";
        return jdbcTemplate.query(sql, thongBaoMapper, userId, searchPattern, searchPattern, size, offset);
    }

    // 16. Lấy thông báo mới nhất (n thông báo)
    public List<ThongBao> getLatestThongBao(int userId, int limit) {
        String sql = "SELECT * FROM ThongBao WHERE MaNguoiDung = ? " +
                "ORDER BY NgayTao DESC LIMIT ?";
        return jdbcTemplate.query(sql, thongBaoMapper, userId, limit);
    }

    // 17. Kiểm tra có thông báo mới không (so với lần cuối check)
    public boolean hasNewNotifications(int userId, String lastCheckTime) {
        String sql = "SELECT COUNT(*) FROM ThongBao WHERE MaNguoiDung = ? " +
                "AND NgayTao > ? AND TrangThai = 'Chưa đọc'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, lastCheckTime);
        return count != null && count > 0;
    }

    // 18. Tạo thông báo cho nhiều user (broadcast)
    public int createBroadcastNotification(List<Integer> userIds, String tieuDe, String noiDung, String loaiThongBao) {
        String sql = "INSERT INTO ThongBao (MaNguoiDung, TieuDe, NoiDung, LoaiThongBao, TrangThai) " +
                "VALUES (?, ?, ?, ?, 'Chưa đọc')";
        int count = 0;
        for (Integer userId : userIds) {
            count += jdbcTemplate.update(sql, userId, tieuDe, noiDung, loaiThongBao);
        }
        return count;
    }

    // 19. Đếm số thông báo theo trạng thái và loại
    public int countByStatusAndType(int userId, String trangThai, String loaiThongBao) {
        String sql = "SELECT COUNT(*) FROM ThongBao WHERE MaNguoiDung = ? " +
                "AND TrangThai = ? AND LoaiThongBao = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, trangThai, loaiThongBao);
    }

    // 20. Lấy thống kê thông báo theo loại
    public List<Object[]> getThongKeTheoLoai(int userId) {
        String sql = "SELECT LoaiThongBao, COUNT(*) as SoLuong, " +
                "SUM(CASE WHEN TrangThai = 'Chưa đọc' THEN 1 ELSE 0 END) as ChuaDoc " +
                "FROM ThongBao WHERE MaNguoiDung = ? " +
                "GROUP BY LoaiThongBao";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[] {
                rs.getString("LoaiThongBao"),
                rs.getInt("SoLuong"),
                rs.getInt("ChuaDoc")
        }, userId);
    }
}