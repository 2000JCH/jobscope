package com.jobscope.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileImageResponse {

    private String profileImage;

    public static ProfileImageResponse of(String profileImage) {
        return ProfileImageResponse.builder()
                .profileImage(profileImage)
                .build();
    }
}