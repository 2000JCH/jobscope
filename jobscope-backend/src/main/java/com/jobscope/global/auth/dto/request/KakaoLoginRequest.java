package com.jobscope.global.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class KakaoLoginRequest {

    @NotBlank
    private String code;
}
