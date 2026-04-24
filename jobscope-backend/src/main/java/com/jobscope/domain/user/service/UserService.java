package com.jobscope.domain.user.service;

import com.jobscope.domain.application.dto.response.JourneyResponse;
import com.jobscope.domain.application.service.AnalyticsService;
import com.jobscope.domain.application.service.ApplicationService;
import com.jobscope.domain.oauth.service.OAuthTokenService;
import com.jobscope.domain.user.dto.request.UpdateUserRequest;
import com.jobscope.domain.user.dto.response.ProfileImageResponse;
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
import com.jobscope.global.infra.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

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
    private final AnalyticsService analyticsService;
    private final S3Service s3Service;

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

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusNanos(jwtProvider.getRefreshTokenExpire() * 1_000_000L);
        user.updateRefreshToken(refreshToken, expiresAt);
        user.updateLastLoginAt();

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

        String newAccessToken = jwtProvider.createAccessToken(userId, user.getRole());
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
        user.updateNickname(request.getNickname());
        log.info("[UserService] 프로필 수정 완료 - userId: {}", userId);
    }

    /**
     * 프로필 이미지를 S3에 업로드하고 URL을 저장한다.
     * 기존 커스텀 이미지가 있으면 S3에서 삭제 후 교체한다.
     *
     * @param userId 인증된 사용자 ID
     * @param file   업로드할 이미지 파일 (jpg, png, 5MB 이하)
     * @return 새 프로필 이미지 URL
     */
    @Transactional
    public ProfileImageResponse updateProfileImage(Long userId, MultipartFile file) {
        User user = findUserById(userId);
        String oldImageUrl = user.getCustomProfileImage();
        String newImageUrl = s3Service.uploadImage("profile", file);
        user.updateProfileImage(newImageUrl);
        if (oldImageUrl != null) {
            s3Service.deleteImage(oldImageUrl);
        }
        log.info("[UserService] 프로필 이미지 업로드 완료 - userId: {}", userId);
        return ProfileImageResponse.of(newImageUrl);
    }

    /**
     * 커스텀 프로필 이미지를 초기화한다 (카카오 기본 이미지로 복귀).
     * S3에 저장된 이미지를 삭제하고 customProfileImage를 null로 설정한다.
     *
     * @param userId 인증된 사용자 ID
     */
    @Transactional
    public void resetProfileImage(Long userId) {
        User user = findUserById(userId);
        String imageUrl = user.getCustomProfileImage();
        if (imageUrl != null) {
            user.clearCustomProfileImage();
            s3Service.deleteImage(imageUrl);
            log.info("[UserService] 프로필 이미지 초기화 완료 - userId: {}", userId);
        }
    }

    /**
     * 회원을 탈퇴 처리한다 (Hard Delete).
     * DB FK ON DELETE CASCADE로 APPLICATION, ALARM_LOG 연관 데이터가 함께 삭제된다.
     *
     * @param userId 인증된 사용자 ID
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = findUserById(userId);
        kakaoAuthService.unlinkKakaoUser(user.getKakaoId());
        if (user.getCustomProfileImage() != null) {
            s3Service.deleteImage(user.getCustomProfileImage());
        }
        applicationService.softDeleteAllByUserId(userId);
        oAuthTokenService.deleteByUserId(userId);
        userRepository.deleteById(userId);
        log.info("[UserService] 회원 탈퇴 완료 - userId: {}", userId);
    }

    /**
     * MY 탭 취준 여정 데이터를 조회한다. AnalyticsService에 위임한다.
     *
     * @param userId 인증된 사용자 ID
     * @return 취준 여정 응답
     */
    public JourneyResponse getJourney(Long userId) {
        return analyticsService.getJourney(userId);
    }

    // ── 관리자 전용 메서드 ────────────────────────────────────────────────

    /**
     * 전체 사용자 수를 반환한다.
     */
    public long getTotalUserCount() {
        return userRepository.count();
    }

    /**
     * 특정 시각 이후 가입한 사용자 수를 반환한다.
     */
    public long getNewUserCountSince(LocalDateTime since) {
        return userRepository.countByCreatedAtAfter(since);
    }

    /**
     * 특정 시각 이후 로그인한 활성 사용자 수를 반환한다.
     */
    public long getActiveUserCountSince(LocalDateTime since) {
        return userRepository.countByLastLoginAtAfter(since);
    }

    /**
     * 특정 시각 이후 일별 신규 가입 수 raw 데이터를 반환한다.
     */
    public List<Object[]> getDailySignupsRaw(LocalDateTime since) {
        return userRepository.findDailySignupsSince(since);
    }

    /**
     * 관리자용 사용자 목록을 페이지로 조회한다 (최신 가입순).
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsersForAdmin(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    /**
     * 관리자가 특정 사용자를 강제 삭제한다.
     */
    @Transactional
    public void deleteUserByAdmin(Long adminId, Long targetUserId) {
        if (adminId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        User user = findUserById(targetUserId);
        kakaoAuthService.unlinkKakaoUser(user.getKakaoId());
        if (user.getCustomProfileImage() != null) {
            s3Service.deleteImage(user.getCustomProfileImage());
        }
        applicationService.softDeleteAllByUserId(targetUserId);
        oAuthTokenService.deleteByUserId(targetUserId);
        userRepository.deleteById(targetUserId);
        log.info("[UserService] 관리자 회원 삭제 완료 - targetUserId: {}", targetUserId);
    }


    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}