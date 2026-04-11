package com.jobscope.global.auth.controller;

import com.jobscope.domain.user.service.UserService;
import com.jobscope.global.auth.dto.request.KakaoLoginRequest;
import com.jobscope.global.auth.dto.request.RefreshTokenRequest;
import com.jobscope.global.auth.dto.response.LoginResponse;
import com.jobscope.global.auth.dto.response.RefreshResponse;
import com.jobscope.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<LoginResponse>> kakaoLogin(
            @RequestBody @Valid KakaoLoginRequest request) {
        LoginResponse response = userService.kakaoLogin(request.getCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(
            @RequestBody @Valid RefreshTokenRequest request) {
        RefreshResponse response = userService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal Long userId) {
        userService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
