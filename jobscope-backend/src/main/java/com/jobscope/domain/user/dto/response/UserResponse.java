package com.jobscope.domain.user.dto.response;

import com.jobscope.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String nickname;
    private String email;
    private String profileImage;
    private String phoneNumber;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
