package com.example.HeThongBanThuoc.service;

import com.example.HeThongBanThuoc.model.TinNhan;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ChatService {

    private final DataSource dataSource;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 60000;
    private static final long SSE_TIMEOUT = 60000;

    private static final String GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash";
    private static final String GEMINI_GENERATE_URL = GEMINI_API_BASE + ":generateContent?key=%s";
    private static final String GEMINI_STREAM_URL = GEMINI_API_BASE + ":streamGenerateContent?key=%s";

    public ChatService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    private static final String SYSTEM_PROMPT = """
            Bạn là **trợ lý tư vấn thuốc thông minh** của hệ thống nhà thuốc.
            Hãy trả lời thân thiện, tự nhiên, chính xác chuyên môn và ngắn gọn.

            Nhiệm vụ:
            Giải đáp thắc mắc về thuốc, liều dùng, tác dụng phụ, tương tác thuốc.
            Hướng dẫn sử dụng thuốc an toàn, tư vấn sức khỏe cơ bản.
            Nếu câu hỏi vượt ngoài chuyên môn (chẩn đoán, kê đơn, bệnh nghiêm trọng) → khuyên khách hàng đến gặp bác sĩ.
            Luôn nhắc: "Thông tin chỉ mang tính tham khảo, không thay thế ý kiến của bác sĩ."
            Luôn trả lời bằng **tiếng Việt**, thân thiện và dễ hiểu.
            """;

    // --- AUTO CLOSE SESSION TASK ---
    @PostConstruct
    public void startAutoCloseScheduler() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60_000); // kiểm tra mỗi phút
                    tuDongKetThucPhienKhongHoatDong();
                } catch (Exception ignored) {
                }
            }
        }, "AutoCloseSessionThread").start();
    }

    private void tuDongKetThucPhienKhongHoatDong() {
        String sql = """
            UPDATE PhienTuVan
            SET TrangThai = 'Đã kết thúc', NgayCapNhat = NOW()
            WHERE TrangThai = 'Đang mở'
            AND TIMESTAMPDIFF(MINUTE, NgayCapNhat, NOW()) >= 3
            """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    // Phiên tư vấn
    public int layHoacTaoPhienTuVan(int maNguoiDung) {
        String sqlCheck = "SELECT MaPhienTuVan FROM PhienTuVan WHERE MaNguoiGui = ? AND TrangThai = 'Đang mở' ORDER BY NgayTao DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlCheck)) {

            ps.setInt(1, maNguoiDung);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("MaPhienTuVan");
            }

            return taoPhienTuVanMoi(conn, maNguoiDung);

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi truy vấn phiên tư vấn: " + e.getMessage(), e);
        }
    }

    public int taoPhienTuVanMoi(Connection conn, int maNguoiDung) throws SQLException {
        String sqlInsert = "INSERT INTO PhienTuVan (MaNguoiGui, TrangThai, NgayTao, NgayCapNhat) VALUES (?, 'Đang mở', NOW(), NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, maNguoiDung);
            ps.executeUpdate();
            ResultSet rsKey = ps.getGeneratedKeys();
            if (rsKey.next()) return rsKey.getInt(1);
            throw new SQLException("Không thể lấy ID phiên tư vấn mới");
        }
    }

    public List<Map<String, Object>> layDanhSachPhienTuVan(int maNguoiDung) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT MaPhienTuVan, TrangThai, NgayTao, NgayCapNhat FROM PhienTuVan WHERE MaNguoiGui = ? ORDER BY NgayTao DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNguoiDung);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("maPhienTuVan", rs.getInt("MaPhienTuVan"));
                row.put("trangThai", rs.getString("TrangThai"));
                row.put("ngayTao", rs.getTimestamp("NgayTao"));
                row.put("ngayCapNhat", rs.getTimestamp("NgayCapNhat"));
                result.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách phiên tư vấn: " + e.getMessage(), e);
        }
        return result;
    }

    public void ketThucPhienTuVan(int maPhienTuVan, int maNguoiDung) {
        String sql = "UPDATE PhienTuVan SET TrangThai = 'Đã kết thúc', NgayCapNhat = NOW() WHERE MaPhienTuVan = ? AND MaNguoiGui = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPhienTuVan);
            ps.setInt(2, maNguoiDung);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết thúc phiên tư vấn: " + e.getMessage(), e);
        }
    }

    public void xoaPhienTuVan(int maPhienTuVan, int maNguoiDung) {
        String sql = "DELETE FROM PhienTuVan WHERE MaPhienTuVan = ? AND MaNguoiGui = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPhienTuVan);
            ps.setInt(2, maNguoiDung);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa phiên tư vấn: " + e.getMessage(), e);
        }
    }

    // Tin nhắn
    public void luuTinNhan(TinNhan tinNhan) {
        String sql = "INSERT INTO TinNhan (MaTuVan, NguoiDung, NoiDung, ThoiGian) VALUES (?, ?, ?, NOW())";
        String updateActivity = "UPDATE PhienTuVan SET NgayCapNhat = NOW() WHERE MaPhienTuVan = ?";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 PreparedStatement up = conn.prepareStatement(updateActivity)) {
                ps.setInt(1, tinNhan.getMaTuVan());
                ps.setString(2, tinNhan.getNguoiDung());
                ps.setString(3, tinNhan.getNoiDung());
                ps.executeUpdate();

                up.setInt(1, tinNhan.getMaTuVan());
                up.executeUpdate();

                conn.commit();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu tin nhắn: " + e.getMessage(), e);
        }
    }


    public List<TinNhan> layLichSuTinNhan(int maPhienTuVan, int limit) {
        List<TinNhan> messages = new ArrayList<>();
        String sql = "SELECT * FROM TinNhan WHERE MaTuVan = ? ORDER BY ThoiGian DESC LIMIT ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPhienTuVan);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TinNhan msg = new TinNhan();
                msg.setMaTinNhan(rs.getInt("MaTinNhan"));
                msg.setMaTuVan(rs.getInt("MaTuVan"));
                msg.setNguoiDung(rs.getString("NguoiDung"));
                msg.setNoiDung(rs.getString("NoiDung"));
                msg.setThoiGian(rs.getTimestamp("ThoiGian").toLocalDateTime());
                messages.add(0, msg);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy lịch sử tin nhắn: " + e.getMessage(), e);
        }
        return messages;
    }

    //  TƯƠNG TÁC VỚI GEMINI API
    /*
     * Gửi câu hỏi đến Gemini AI và nhận phản hồi đồng bộ (không stream)
     * @param cauHoi Câu hỏi của người dùng
     * @param maPhienTuVan ID phiên tư vấn (để lấy context)
     * @return Câu trả lời từ AI
     */
    public String guiCauHoiToiAI(String cauHoi, int maPhienTuVan) {
        try {
            // Xây dựng context từ lịch sử hội thoại
            String context = xayDungContextHoiThoai(cauHoi, maPhienTuVan);

            // Gọi Gemini API
            String apiUrl = String.format(GEMINI_GENERATE_URL, geminiApiKey);
            HttpURLConnection conn = taoKetNoiAPI(apiUrl);

            // Gửi request
            guiRequestJSON(conn, context);

            // Xử lý response
            int code = conn.getResponseCode();
            if (code != 200) {
                return xuLyLoiAPI(conn, code);
            }

            String response = docResponse(conn);
            return traiXuatTextTuJSON(response);

        } catch (Exception e) {
            return "Lỗi kết nối AI: " + e.getMessage();
        }
    }

    /*
     * Stream phản hồi AI theo thời gian thực qua SSE
     * @param cauHoi Câu hỏi của người dùng
     * @param maPhienTuVan ID phiên tư vấn
     * @return SseEmitter để client nhận stream
     */
    public SseEmitter streamAIResponse(String cauHoi, int maPhienTuVan) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // Giữ SecurityContext để thread mới có quyền truy cập DB
        var securityContext = org.springframework.security.core.context.SecurityContextHolder.getContext();

        // Tạo task xử lý stream trong thread riêng
        Runnable task = () -> xuLyStreamAI(cauHoi, maPhienTuVan, emitter);

        // Chạy task với SecurityContext
        Runnable secureTask = new DelegatingSecurityContextRunnable(task, securityContext);
        new Thread(secureTask).start();

        return emitter;
    }

    //  PRIVATE HELPER METHODS

    /*
     * Xây dựng context hội thoại từ system prompt + lịch sử + câu hỏi hiện tại
     */
    private String xayDungContextHoiThoai(String cauHoi, int maPhienTuVan) {
        List<TinNhan> lichSu = layLichSuTinNhan(maPhienTuVan, 10);
        StringBuilder context = new StringBuilder(SYSTEM_PROMPT).append("\n\n");

        for (TinNhan msg : lichSu) {
            context.append(msg.getNguoiDung()).append(": ").append(msg.getNoiDung()).append("\n");
        }
        context.append("Khách hàng: ").append(cauHoi);

        return context.toString();
    }

    /*
     * Tạo HTTP connection tới Gemini API
     */
    private HttpURLConnection taoKetNoiAPI(String apiUrl) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        return conn;
    }

    /*
     * Gửi request JSON đến API
     */
    private void guiRequestJSON(HttpURLConnection conn, String context) throws IOException {
        String jsonBody = String.format("""
                {
                  "contents": [
                    {
                      "role": "user",
                      "parts": [{"text": "%s"}]
                    }
                  ]
                }
                """,
                context.replace("\"", "\\\"").replace("\n", "\\n"));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }
    }

    /*
     * Đọc response từ API
     */
    private String docResponse(HttpURLConnection conn) throws IOException {
        try (InputStream is = conn.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /*
     * Xử lý lỗi từ API
     */
    private String xuLyLoiAPI(HttpURLConnection conn, int code) throws IOException {
        String err;
        try (InputStream errStream = conn.getErrorStream()) {
            err = errStream != null ? new String(errStream.readAllBytes(), StandardCharsets.UTF_8) : "Unknown error";
        }
        throw new RuntimeException("Gemini API lỗi (" + code + "): " + err);
    }

    /*
     * Trích xuất text từ JSON response của Gemini
     */
    private String traiXuatTextTuJSON(String response) throws IOException {
        JsonNode root = objectMapper.readTree(response);
        return root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();
    }

    /*
     * Xử lý stream AI trong thread riêng
     */
    private void xuLyStreamAI(String cauHoi, int maPhienTuVan, SseEmitter emitter) {
        HttpURLConnection conn = null;
        try {
            // Xây dựng context
            String context = xayDungContextHoiThoai(cauHoi, maPhienTuVan);

            // Kết nối API stream
            String apiUrl = String.format(GEMINI_STREAM_URL, geminiApiKey);
            conn = taoKetNoiAPI(apiUrl);
            guiRequestJSON(conn, context);

            // Kiểm tra response code
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                guiLoiSSE(emitter, "Lỗi Gemini API (" + responseCode + ")");
                return;
            }

            // Đọc và parse stream
            String fullText = docVaParseStreamJSON(conn, emitter);

            // Lưu vào DB nếu có nội dung
            if (fullText.isEmpty()) {
                emitter.send(" AI không trả về phản hồi. Vui lòng thử lại.");
            } else {
                TinNhan aiMsg = new TinNhan(maPhienTuVan, "AI", fullText);
                luuTinNhan(aiMsg);
            }

            // Kết thúc stream
            emitter.send("[DONE]");
            emitter.complete();

        } catch (IOException e) {
            guiLoiSSE(emitter, "Lỗi kết nối: " + e.getMessage());
            emitter.completeWithError(e);
        } catch (Exception e) {
            guiLoiSSE(emitter, "Lỗi không xác định: " + e.getMessage());
            emitter.completeWithError(e);
        } finally {
            if (conn != null) conn.disconnect();
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    /*
     * Đọc stream JSON từ Gemini và gửi từng chunk qua SSE
     * @return Text đầy đủ từ AI
     */
    private String docVaParseStreamJSON(HttpURLConnection conn, SseEmitter emitter) throws IOException {
        StringBuilder fullStream = new StringBuilder();
        StringBuilder fullText = new StringBuilder();

        // Đọc toàn bộ stream
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fullStream.append(line);
            }
        }

        // Parse JSON array
        try {
            JsonNode rootArray = objectMapper.readTree(fullStream.toString());

            if (rootArray.isArray()) {
                for (JsonNode item : rootArray) {
                    String token = traiXuatTokenTuNode(item);

                    if (!token.isEmpty()) {
                        fullText.append(token);
                        emitter.send(token); // Gửi chunk đến client
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Parse JSON error: " + e.getMessage());
        }

        return fullText.toString();
    }

    /*
     * Trích xuất token text từ JsonNode
     */
    private String traiXuatTokenTuNode(JsonNode item) {
        JsonNode candidates = item.path("candidates");

        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode parts = candidates.get(0).path("content").path("parts");

            if (parts.isArray() && parts.size() > 0) {
                return parts.get(0).path("text").asText("");
            }
        }
        return "";
    }

    /*
     * Gửi lỗi qua SSE
     */
    private void guiLoiSSE(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name("error").data(message));
            emitter.complete();
        } catch (IOException ignored) {}
    }
}