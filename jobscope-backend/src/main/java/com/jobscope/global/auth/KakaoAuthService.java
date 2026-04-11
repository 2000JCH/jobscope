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
     * 카카오 인가코드로 카카오 액세스 토큰을 교환한다.
     *
     * @param code 카카오 OAuth 인가코드
     * @return 카카오 액세스 토큰
     * @throws BusinessException 카카오 API 호출 실패 시 (KAKAO_AUTH_FAILED)
     */
    public String getKakaoAccessToken(String code) {
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

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("access_token")) {
                throw new BusinessException(ErrorCode.KAKAO_AUTH_FAILED);
            }

            return (String) body.get("access_token");

        } catch (RestClientException e) {
            log.error("[KakaoAuthService] 카카오 토큰 교환 실패 - {}", e.getMessage(), e);
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

}
