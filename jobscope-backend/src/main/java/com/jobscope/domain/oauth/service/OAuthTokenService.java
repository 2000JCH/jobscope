package com.jobscope.domain.oauth.service;

import com.jobscope.domain.oauth.entity.OAuthToken;
import com.jobscope.domain.oauth.repository.OAuthTokenRepository;
import com.jobscope.global.auth.KakaoAuthService;
import com.jobscope.global.auth.KakaoTokenInfo;
import com.jobscope.global.common.exception.BusinessException;
import com.jobscope.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OAuthTokenService {

    private final OAuthTokenRepository oAuthTokenRepository;
    private final KakaoAuthService kakaoAuthService;

    private static final String KAKAO = "KAKAO";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * 카카오 로그인 시 OAuth 토큰을 저장하거나 갱신한다 (upsert).
     * 기존 토큰이 있으면 access_token을 갱신하고, refresh_token은 값이 있을 때만 갱신한다.
     *
     * @param userId    유저 ID
     * @param tokenInfo 카카오 토큰 교환 결과
     */
    @Transactional
    public void saveOrUpdate(Long userId, KakaoTokenInfo tokenInfo) {
        oAuthTokenRepository.findByUserIdAndProvider(userId, KAKAO)
                .ifPresentOrElse(
                        token -> {
                            token.updateAccessToken(tokenInfo.accessToken(), tokenInfo.accessTokenExpiresAt());
                            if (tokenInfo.refreshToken() != null) {
                                token.updateRefreshToken(tokenInfo.refreshToken(), tokenInfo.refreshTokenExpiresAt());
                            }
                        },
                        () -> oAuthTokenRepository.save(
                                OAuthToken.builder()
                                        .userId(userId)
                                        .provider(KAKAO)
                                        .accessToken(tokenInfo.accessToken())
                                        .accessTokenExpiresAt(tokenInfo.accessTokenExpiresAt())
                                        .refreshToken(tokenInfo.refreshToken())
                                        .refreshTokenExpiresAt(tokenInfo.refreshTokenExpiresAt())
                                        .build()
                        )
                );
        log.info("[OAuthTokenService] 카카오 토큰 저장/갱신 완료 - userId: {}", userId);
    }

    /**
     * 유저의 OAuth 토큰을 삭제한다. 회원탈퇴 시 호출된다.
     *
     * @param userId 유저 ID
     */
    @Transactional
    public void deleteByUserId(Long userId) {
        oAuthTokenRepository.deleteByUserId(userId);
        log.info("[OAuthTokenService] OAuth 토큰 삭제 완료 - userId: {}", userId);
    }

    /**
     * 유효한 카카오 액세스 토큰을 반환한다.
     * 만료 5분 이내이거나 만료된 경우 refresh_token으로 갱신 후 반환한다.
     * 갱신 응답에 새 refresh_token이 포함된 경우에만 DB를 업데이트한다 (카카오 정책).
     *
     * @param userId 유저 ID
     * @return 유효한 카카오 액세스 토큰
     * @throws BusinessException OAuth 토큰 없음 또는 갱신 실패 시 (ALARM_SEND_FAILED)
     */
    @Transactional
    public String getValidAccessToken(Long userId) {
        OAuthToken token = oAuthTokenRepository.findByUserIdAndProvider(userId, KAKAO)
                .orElseThrow(() -> {
                    log.warn("[OAuthTokenService] OAuth 토큰 없음 - userId: {}", userId);
                    return new BusinessException(ErrorCode.ALARM_SEND_FAILED);
                });

        // NOTE: 5분 버퍼 — 만료 5분 이내이면 갱신 처리하여 발송 직전 만료 방지
        if (token.getAccessTokenExpiresAt() != null
                && token.getAccessTokenExpiresAt().isAfter(LocalDateTime.now().plusMinutes(5))) {
            return token.getAccessToken();
        }

        // NOTE: refresh_token null 또는 만료 시 사용자가 재로그인해야 새 토큰이 발급됨
        if (token.getRefreshToken() == null) {
            log.warn("[OAuthTokenService] refresh_token 없음 — 재로그인 필요 - userId: {}", userId);
            throw new BusinessException(ErrorCode.ALARM_SEND_FAILED);
        }
        if (token.getRefreshTokenExpiresAt() != null
                && token.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now(KST))) {
            log.warn("[OAuthTokenService] refresh_token 만료 — 재로그인 필요 - userId: {}", userId);
            throw new BusinessException(ErrorCode.ALARM_SEND_FAILED);
        }

        // NOTE: Phase 1 허용 범위 — AlarmScheduler(내부 스케줄러)에서만 호출되므로
        // @Transactional 내 HTTP 호출로 인한 커넥션 점유가 실사용에 영향 없음
        log.info("[OAuthTokenService] 카카오 액세스 토큰 만료 — 갱신 처리 - userId: {}", userId);
        KakaoTokenInfo refreshed;
        try {
            refreshed = kakaoAuthService.refreshKakaoToken(token.getRefreshToken());
        } catch (BusinessException e) {
            log.error("[OAuthTokenService] 카카오 토큰 갱신 실패 - userId: {}", userId);
            throw new BusinessException(ErrorCode.ALARM_SEND_FAILED);
        }

        token.updateAccessToken(refreshed.accessToken(), refreshed.accessTokenExpiresAt());
        // NOTE: refresh_token은 카카오 정책상 잔여 30일 미만일 때만 응답에 포함됨
        if (refreshed.refreshToken() != null) {
            token.updateRefreshToken(refreshed.refreshToken(), refreshed.refreshTokenExpiresAt());
            log.info("[OAuthTokenService] 카카오 리프레시 토큰 갱신 완료 - userId: {}", userId);
        }

        log.info("[OAuthTokenService] 카카오 액세스 토큰 갱신 완료 - userId: {}", userId);
        return refreshed.accessToken();
    }
}