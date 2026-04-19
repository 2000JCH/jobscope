package com.jobscope.domain.user.controller;

import com.jobscope.domain.user.dto.request.UpdateUserRequest;
import com.jobscope.domain.user.dto.response.ProfileImageResponse;
import com.jobscope.domain.user.dto.response.UserResponse;
import com.jobscope.domain.user.service.UserService;
import com.jobscope.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProfileImageResponse>> uploadProfileImage(
            @AuthenticationPrincipal Long userId,
            @RequestParam("image") MultipartFile file) {
        ProfileImageResponse response = userService.updateProfileImage(userId, file);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<Void>> resetProfileImage(
            @AuthenticationPrincipal Long userId) {
        userService.resetProfileImage(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
