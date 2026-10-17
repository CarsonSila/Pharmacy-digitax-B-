package com.example.HeThongBanThuoc.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    // Khóa bí mật dùng để ký JWT (phải đủ dài, ít nhất 32 ký tự)
    private static final String SECRET = "my-super-strong-secret-key-256-bit-length!@#123456";

    // Tạo key từ chuỗi SECRET dùng thuật toán HMAC-SHA256
    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Thời gian hết hạn Access Token: 1 giờ (3600000 ms)
    @Value("${jwt.access-token-expiration:3600000}")
    private long accessTokenExpiration;

    // Thời gian hết hạn Refresh Token: 7 ngày (604800000 ms)
    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    /**
     * Tạo Access Token (thời gian sống ngắn - 1 giờ)
     * Chứa: email, role, userId
     */
    public String generateToken(String email, String role, Integer userId) {
        Date now = new Date(); // thời điểm hiện tại
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration); // thời điểm hết hạn

        // Xây dựng JWT
        return Jwts.builder()
                .setSubject(email) // đặt subject là email
                .addClaims(Map.of(
                        "role", role,     // thêm thông tin role vào payload
                        "userId", userId , // thêm userId vào payload
                        "type", "access"
                ))
                .setIssuedAt(now)          // thời gian phát hành
                .setExpiration(expiryDate) // thời gian hết hạn
                .signWith(key, SignatureAlgorithm.HS256) // ký token bằng HS256 và key
                .compact(); // tạo chuỗi token hoàn chỉnh
    }

    /**
     * Tạo Refresh Token (thời gian sống dài - 7 ngày)
     * Chứa: email, type = "refresh"
     */
    public String generateRefreshToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(email)
                .addClaims(Map.of("type", "refresh"))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Lấy email từ token
    public String getEmailFromToken(String token) {
        return parseAllClaims(token).getSubject(); // subject trong token là email
    }

    // Lấy role từ token
    public String getRoleFromToken(String token) {
        return parseAllClaims(token).get("role", String.class); // đọc claim "role"
    }

    // Lấy userId từ token
    public Integer getUserIdFromToken(String token) {
        return parseAllClaims(token).get("userId", Integer.class); // đọc claim "userId"
    }

    // Parse tất cả claims từ token (bao gồm subject, role, userId, expiration...)
    private Claims parseAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // sử dụng key để xác thực chữ ký
                .build()
                .parseClaimsJws(token) // parse token
                .getBody(); // trả về payload (claims)
    }

    // Kiểm tra token có hợp lệ không
    public boolean validateToken(String token) {
        try {
            parseAllClaims(token); // nếu parse thành công → token hợp lệ
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Nếu parse lỗi hoặc token bị thay đổi → không hợp lệ
            System.out.println("JWT không hợp lệ: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra xem token có phải là Refresh Token không
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseAllClaims(token);
            String type = claims.get("type", String.class);
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Lấy thời gian hết hạn của Access Token (tính bằng giây)
     */
    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
    }
}
