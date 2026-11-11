package com.example.HeThongBanThuoc.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    // Đọc secret từ biến môi trường / application.properties — KHÔNG hardcode
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Signing key — được khởi tạo sau khi @Value được inject
    private Key key;

    // Thời gian hết hạn Access Token: 1 giờ (3600000 ms)
    @Value("${jwt.access-token-expiration:3600000}")
    private long accessTokenExpiration;

    // Thời gian hết hạn Refresh Token: 7 ngày (604800000 ms)
    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalStateException(
                "JWT secret (jwt.secret) phải có ít nhất 32 ký tự. " +
                "Hãy đặt biến môi trường JWT_SECRET với giá trị ngẫu nhiên dài."
            );
        }
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        log.info("JwtTokenProvider đã khởi tạo thành công.");
    }

    /**
     * Tạo Access Token (thời gian sống ngắn - 1 giờ)
     * Chứa: email, role, userId
     */
    public String generateToken(String email, String role, Integer userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(email)
                .addClaims(Map.of(
                        "role", role,
                        "userId", userId,
                        "type", "access"
                ))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
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
        return parseAllClaims(token).getSubject();
    }

    // Lấy role từ token
    public String getRoleFromToken(String token) {
        return parseAllClaims(token).get("role", String.class);
    }

    // Lấy userId từ token
    public Integer getUserIdFromToken(String token) {
        return parseAllClaims(token).get("userId", Integer.class);
    }

    // Parse tất cả claims từ token
    private Claims parseAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Kiểm tra token có hợp lệ không
    public boolean validateToken(String token) {
        try {
            parseAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT không hợp lệ: {}", e.getMessage());
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
