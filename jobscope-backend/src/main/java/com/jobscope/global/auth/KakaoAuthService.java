package com.jobscope.global.auth;

import com.jobscope.global.common.exception.BusinessException;
import com.jobscope.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_URL = "https://kapi.kakao.com/v2/user/me";

    private final RestTemplate restTemplate;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 카카오 인가코드로 액세스 토큰·리프레시 토큰을 교환한다.
     *
     * @param code 카카오 OAuth 인가코드
     * @return KakaoTokenInfo (accessToken, accessTokenExpiresAt, refreshToken, refreshTokenExpiresAt)
     * @throws BusinessException 카카오 API 호출 실패 시 (KAKAO_AUTH_FAILED)
     */
    public KakaoTokenInfo exchangeKakaoToken(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    KAKAO_TOKEN_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(params, headers),
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            return parseTokenResponse(response.getBody(), ErrorCode.KAKAO_AUTH_FAILED);

        } catch (RestClientException e) {
            log.error("[KakaoAuthService] 카카오 토큰 교환 실패 - {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }

    /**
     * 카카오 리프레시 토큰으로 액세스 토큰을 갱신한다.
     * 카카오 정책상 리프레시 토큰 잔여 30일 미만일 때만 새 refresh_token이 응답에 포함된다.
     *
     * @param refreshToken 기존 카카오 리프레시 토큰
     * @return KakaoTokenInfo (refreshToken, refreshTokenExpiresAt은 갱신 없으면 null)
     * @throws BusinessException 카카오 API 호출 실패 시 (ALARM_SEND_FAILED)
     */
    public KakaoTokenInfo refreshKakaoToken(String refreshToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "refresh_token");
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("refresh_token", refreshToken);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    KAKAO_TOKEN_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(params, headers),
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            return parseTokenResponse(response.getBody(), ErrorCode.KAKAO_AUTH_FAILED);

        } catch (RestClientException e) {
            log.error("[KakaoAuthService] 카카오 토큰 갱신 실패 - {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }

    /**
     * 카카오 액세스 토큰으로 카카오 사용자 정보를 조회한다.
     *
     * @param kakaoAccessToken 카카오 액세스 토큰
     * @return kakaoId, nickname, email, profileImage가 담긴 KakaoUserInfo
     * @throws BusinessException 카카오 API 호출 실패 시 (KAKAO_AUTH_FAILED)
     */
    public KakaoUserInfo getKakaoUserInfo(String kakaoAccessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(kakaoAccessToken);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    KAKAO_USER_URL,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
            }

            String kakaoId = String.valueOf(body.get("id"));
            Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
            Map<String, Object> profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;

            String nickname = profile != null ? (String) profile.get("nickname") : "";
            String profileImage = profile != null ? (String) profile.get("profile_image_url") : null;
            String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

            return new KakaoUserInfo(kakaoId, nickname, email, profileImage);

        } catch (RestClientException e) {
            log.error("[KakaoAuthService] 카카오 유저 정보 조회 실패 - {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
        }
    }

    /**
     * 카카오 토큰 API 응답을 KakaoTokenInfo로 파싱한다.
     * refresh_token은 응답에 키가 존재할 때만 파싱하며, 없으면 null로 반환한다.
     */
    private KakaoTokenInfo parseTokenResponse(Map<String, Object> body, ErrorCode errorCode) {
        if (body == null || !body.containsKey("access_token")) {
            throw new BusinessException(errorCode);
        }

        String accessToken = (String) body.get("access_token");
        int expiresIn = (int) body.get("expires_in");
        LocalDateTime accessTokenExpiresAt = LocalDateTime.now().plusSeconds(expiresIn);

        // NOTE: refresh_token은 초기 발급 시 항상 포함, refresh 시 잔여 30일 미만일 때만 포함 (카카오 정책)
        String newRefreshToken = (String) body.get("refresh_token");
        LocalDateTime refreshTokenExpiresAt = null;
        if (newRefreshToken != null && body.containsKey("refresh_token_expires_in")) {
            int refreshExpiresIn = (int) body.get("refresh_token_expires_in");
            refreshTokenExpiresAt = LocalDateTime.now().plusSeconds(refreshExpiresIn);
        }

        return new KakaoTokenInfo(accessToken, accessTokenExpiresAt, newRefreshToken, refreshTokenExpiresAt);
    }
}