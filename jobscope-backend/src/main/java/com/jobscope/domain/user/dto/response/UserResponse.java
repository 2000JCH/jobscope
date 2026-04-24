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
    private Boolean hasCustomProfileImage;

    public static UserResponse from(User user) {
        String effectiveProfileImage = user.getCustomProfileImage() != null
                ? user.getCustomProfileImage()
                : user.getProfileImage();
        return UserResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImage(effectiveProfileImage)
                .hasCustomProfileImage(user.getCustomProfileImage() != null)
                .build();
    }
}
