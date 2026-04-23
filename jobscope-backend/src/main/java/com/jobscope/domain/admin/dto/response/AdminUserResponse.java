package com.jobscope.domain.admin.dto.response;

import com.jobscope.domain.user.entity.User;
import com.jobscope.domain.user.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserResponse {

    private Long id;
    private String nickname;
    private String email;
    private String kakaoId;
    private UserRole role;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    public static AdminUserResponse from(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .kakaoId(user.getKakaoId())
                .role(user.getRole())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}