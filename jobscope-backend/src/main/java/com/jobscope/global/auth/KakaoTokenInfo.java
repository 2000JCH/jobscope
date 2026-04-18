package com.jobscope.global.auth;

import java.time.LocalDateTime;

/**
 * 카카오 토큰 교환/갱신 결과를 담는 레코드.
 * refreshToken, refreshTokenExpiresAt은 refresh_token 갱신 시에만 값이 있고,
 * 갱신 없이 access_token만 재발급된 경우 null이다.
 */
public record KakaoTokenInfo(
        String accessToken,
        LocalDateTime accessTokenExpiresAt,
        String refreshToken,
        LocalDateTime refreshTokenExpiresAt
) {
}