package com.jobscope.domain.user.entity;

import com.jobscope.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String kakaoId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String profileImage;

    @Column(length = 500)
    private String customProfileImage;

    @Column(length = 500)
    private String refreshToken;

    private LocalDateTime refreshTokenExpiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private UserRole role = UserRole.USER;

    private LocalDateTime lastLoginAt;

    /**
     * 프로필 정보를 수정한다. null인 필드는 변경하지 않는다.
     *
     * @param nickname    변경할 닉네임 (null이면 유지)
     * @param phoneNumber 변경할 전화번호 (null이면 유지)
     */
    public void updateNickname(String nickname) {
        if (nickname != null) {
            this.nickname = nickname;
        }
    }

    /**
     * 카카오 로그인 시 닉네임·프로필 이미지를 카카오 최신 정보로 동기화한다.
     *
     * @param nickname     카카오 닉네임
     * @param profileImage 카카오 프로필 이미지 URL
     */
    public void syncKakaoProfile(String nickname, String profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    /**
     * Refresh Token과 만료 일시를 갱신한다.
     *
     * @param refreshToken  새로 발급된 Refresh Token
     * @param expiresAt     Refresh Token 만료 일시
     */
    public void updateRefreshToken(String refreshToken, LocalDateTime expiresAt) {
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresAt = expiresAt;
    }

    /**
     * S3에 업로드한 커스텀 프로필 이미지 URL을 저장한다.
     *
     * @param imageUrl S3 이미지 URL
     */
    public void updateProfileImage(String imageUrl) {
        this.customProfileImage = imageUrl;
    }

    /**
     * 커스텀 프로필 이미지를 초기화한다 (카카오 기본 이미지로 복귀).
     */
    public void clearCustomProfileImage() {
        this.customProfileImage = null;
    }

    /**
     * 로그아웃 처리 — Refresh Token과 만료 일시를 null로 초기화한다.
     */
    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiresAt = null;
    }

    /**
     * 마지막 로그인 시각을 현재 시각으로 갱신한다.
     */
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}