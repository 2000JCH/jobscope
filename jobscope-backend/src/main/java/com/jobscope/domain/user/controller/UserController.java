package com.jobscope.domain.user.controller;

import com.jobscope.domain.user.dto.request.UpdateUserRequest;
import com.jobscope.domain.user.dto.response.UserResponse;
import com.jobscope.domain.user.service.UserService;
import com.jobscope.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getMyProfile(userId)));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid UpdateUserRequest request) {
        userService.updateMyProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
