package com.jobscope.global.auth;

public record KakaoUserInfo(
        String kakaoId,
        String nickname,
        String email,
        String profileImage
) { }
