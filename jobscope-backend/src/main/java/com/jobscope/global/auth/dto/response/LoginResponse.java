package com.jobscope.global.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;

    @JsonIgnore
    private String refreshToken;

    private LoginUserResponse user;

    public LoginResponse withoutRefreshToken() {
        return LoginResponse.builder()
                .accessToken(this.accessToken)
                .user(this.user)
                .build();
    }
}
