package com.jobscope.global.auth;

import com.jobscope.domain.user.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expire}")
    private long accessTokenExpire;

    @Value("${jwt.refresh-token-expire}")
    private long refreshTokenExpire;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token을 생성한다. role claim을 포함한다.
     *
     * @param userId 사용자 ID (subject로 저장)
     * @param role   사용자 역할
     * @return 생성된 Access Token
     */
    public String createAccessToken(Long userId, UserRole role) {
        return buildToken(userId, role, accessTokenExpire);
    }

    /**
     * Refresh Token을 생성한다.
     *
     * @param userId 사용자 ID (subject로 저장)
     * @return 생성된 Refresh Token
     */
    public String createRefreshToken(Long userId) {
        return buildToken(userId, refreshTokenExpire);
    }

    /**
     * 토큰에서 사용자 ID를 추출한다.
     *
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long getUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    /**
     * 토큰에서 role claim을 추출한다. claim이 없으면 null을 반환한다.
     *
     * @param token JWT 토큰
     * @return role 문자열 (없으면 null)
     */
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * 토큰 유효성을 검증한다.
     *
     * @param token JWT 토큰
     * @return 유효하면 true, 만료·위변조 등 무효하면 false
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("[JwtProvider] 유효하지 않은 토큰 - {}", e.getMessage());
            return false;
        }
    }

    /**
     * Refresh Token 만료까지 남은 밀리초를 반환한다.
     *
     * @return Refresh Token 만료 시간 (ms)
     */
    public long getRefreshTokenExpire() {
        return refreshTokenExpire;
    }

    private String buildToken(Long userId, UserRole role, long expireMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role != null ? role.name() : UserRole.USER.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMs))
                .signWith(secretKey)
                .compact();
    }

    private String buildToken(Long userId, long expireMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMs))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}