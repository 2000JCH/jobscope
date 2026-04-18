package com.jobscope.domain.user.service;

import com.jobscope.domain.application.service.ApplicationService;
import com.jobscope.domain.oauth.service.OAuthTokenService;
import com.jobscope.domain.user.dto.request.UpdateUserRequest;
import com.jobscope.domain.user.dto.response.UserResponse;
import com.jobscope.domain.user.entity.User;
import com.jobscope.domain.user.repository.UserRepository;
import com.jobscope.global.auth.JwtProvider;
import com.jobscope.global.auth.KakaoAuthService;
import com.jobscope.global.auth.KakaoTokenInfo;
import com.jobscope.global.auth.KakaoUserInfo;
import com.jobscope.global.auth.dto.response.LoginResponse;
import com.jobscope.global.auth.dto.response.LoginUserResponse;
import com.jobscope.global.auth.dto.response.RefreshResponse;
import com.jobscope.global.common.exception.BusinessException;
import com.jobscope.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final KakaoAuthService kakaoAuthService;
    private final OAuthTokenService oAuthTokenService;
    private final ApplicationService applicationService;

    /**
     * 카카오 인가코드로 로그인 또는 회원가입을 처리하고 JWT를 발급한다.
     * 신규 유저면 INSERT, 기존 유저면 닉네임·프로필 이미지를 카카오 최신 정보로 동기화한다.
     *
     * @param code 카카오 OAuth 인가코드
     * @return accessToken, refreshToken, 유저 기본 정보
     */
    @Transactional
    public LoginResponse kakaoLogin(String code) {
        KakaoTokenInfo kakaoTokenInfo = kakaoAuthService.exchangeKakaoToken(code);
        KakaoUserInfo kakaoUserInfo = kakaoAuthService.getKakaoUserInfo(kakaoTokenInfo.accessToken());

        User user = userRepository.findByKakaoId(kakaoUserInfo.kakaoId())
                .map(existing -> {
                    existing.syncKakaoProfile(kakaoUserInfo.nickname(), kakaoUserInfo.profileImage());
                    return existing;
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .kakaoId(kakaoUserInfo.kakaoId())
                                .nickname(kakaoUserInfo.nickname())
                                .email(kakaoUserInfo.email())
                                .profileImage(kakaoUserInfo.profileImage())
                                .build()
                ));

        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusNanos(jwtProvider.getRefreshTokenExpire() * 1_000_000L);
        user.updateRefreshToken(refreshToken, expiresAt);

        oAuthTokenService.saveOrUpdate(user.getId(), kakaoTokenInfo);

        log.info("[UserService] 카카오 로그인 완료 - userId: {}", user.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(LoginUserResponse.from(user))
                .build();
    }

    /**
     * Refresh Token으로 새 Access Token을 재발급한다.
     * Refresh Token 자체는 갱신하지 않는다.
     *
     * @param refreshToken 기존 Refresh Token
     * @return 새로 발급된 Access Token
     * @throws BusinessException 토큰 무효 또는 만료 시 (INVALID_TOKEN)
     */
    @Transactional
    public RefreshResponse refreshToken(String refreshToken) {
        if (refreshToken == null || !jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);
        User user = findUserById(userId);

        if (!refreshToken.equals(user.getRefreshToken())
                || user.getRefreshTokenExpiresAt() == null
                || user.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtProvider.createAccessToken(userId);
        log.info("[UserService] Access Token 재발급 완료 - userId: {}", userId);

        return RefreshResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    /**
     * 로그아웃 처리 — Refresh Token을 무효화한다.
     *
     * @param userId 인증된 사용자 ID
     */
    @Transactional
    public void logout(Long userId) {
        User user = findUserById(userId);
        user.clearRefreshToken();
        log.info("[UserService] 로그아웃 완료 - userId: {}", userId);
    }

    /**
     * 내 프로필을 조회한다.
     *
     * @param userId 인증된 사용자 ID
     * @return 유저 프로필 응답
     */
    public UserResponse getMyProfile(Long userId) {
        return UserResponse.from(findUserById(userId));
    }

    /**
     * 프로필을 수정한다. null 필드는 변경하지 않는다.
     *
     * @param userId  인증된 사용자 ID
     * @param request 수정할 nickname, phoneNumber
     */
    @Transactional
    public void updateMyProfile(Long userId, UpdateUserRequest request) {
        User user = findUserById(userId);
        user.updateProfile(request.getNickname(), request.getPhoneNumber());
        log.info("[UserService] 프로필 수정 완료 - userId: {}", userId);
    }

    /**
     * 회원을 탈퇴 처리한다 (Hard Delete).
     * DB FK ON DELETE CASCADE로 APPLICATION, ALARM_LOG 연관 데이터가 함께 삭제된다.
     *
     * @param userId 인증된 사용자 ID
     */
    @Transactional
    public void deleteUser(Long userId) {
        findUserById(userId);
        applicationService.softDeleteAllByUserId(userId);
        oAuthTokenService.deleteByUserId(userId);
        userRepository.deleteById(userId);
        log.info("[UserService] 회원 탈퇴 완료 - userId: {}", userId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
