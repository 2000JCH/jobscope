package com.jobscope.domain.admin.controller;

import com.jobscope.domain.admin.dto.response.AdminUserResponse;
import com.jobscope.domain.user.entity.User;
import com.jobscope.domain.user.service.UserService;
import com.jobscope.global.common.response.ApiResponse;
import com.jobscope.global.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminUserResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<User> userPage = userService.getAllUsersForAdmin(page, size);
        PageResponse<AdminUserResponse> response = PageResponse.from(userPage.map(AdminUserResponse::from));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal Long adminId,
            @PathVariable Long userId) {
        userService.deleteUserByAdmin(adminId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}