package com.jobscope.domain.user.entity;

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

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 500)
    private String refreshToken;

    private LocalDateTime refreshTokenExpiresAt;

    /**
     * 프로필 정보를 수정한다. null인 필드는 변경하지 않는다.
     *
     * @param nickname    변경할 닉네임 (null이면 유지)
     * @param phoneNumber 변경할 전화번호 (null이면 유지)
     */
    public void updateProfile(String nickname, String phoneNumber) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
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
     * 로그아웃 처리 — Refresh Token과 만료 일시를 null로 초기화한다.
     */
    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiresAt = null;
    }
}