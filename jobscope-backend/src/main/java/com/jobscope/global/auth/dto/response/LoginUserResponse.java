package com.jobscope.global.auth.dto.response;

import com.jobscope.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginUserResponse {

    private Long id;
    private String nickname;
    private String profileImage;

    public static LoginUserResponse from(User user) {
        return LoginUserResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .build();
    }
}
