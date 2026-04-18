package com.jobscope.domain.oauth.entity;

import com.jobscope.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "oauth_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class OAuthToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(length = 500)
    private String accessToken;

    private LocalDateTime accessTokenExpiresAt;

    @Column(length = 500)
    private String refreshToken;

    private LocalDateTime refreshTokenExpiresAt;

    /**
     * 카카오 액세스 토큰과 만료 일시를 갱신한다.
     *
     * @param accessToken  새 카카오 액세스 토큰
     * @param expiresAt    액세스 토큰 만료 일시
     */
    public void updateAccessToken(String accessToken, LocalDateTime expiresAt) {
        this.accessToken = accessToken;
        this.accessTokenExpiresAt = expiresAt;
    }

    /**
     * 카카오 리프레시 토큰과 만료 일시를 갱신한다.
     * 카카오 정책상 잔여 30일 미만일 때만 새 refresh_token이 내려오므로, 응답에 포함된 경우에만 호출한다.
     *
     * @param refreshToken 새 카카오 리프레시 토큰
     * @param expiresAt    리프레시 토큰 만료 일시
     */
    public void updateRefreshToken(String refreshToken, LocalDateTime expiresAt) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = expiresAt;
    }
}