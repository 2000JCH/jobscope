package com.jobscope.global.auth.dto.response;

import com.jobscope.domain.user.entity.User;
import com.jobscope.domain.user.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginUserResponse {

    private Long id;
    private String nickname;
    private String profileImage;
    private UserRole role;

    public static LoginUserResponse from(User user) {
        return LoginUserResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .role(user.getRole())
                .build();
    }
}
