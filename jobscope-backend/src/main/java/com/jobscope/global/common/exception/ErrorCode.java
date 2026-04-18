package com.jobscope.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 인증
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(401, "만료된 토큰입니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),

    // 유저
    USER_NOT_FOUND(404, "유저를 찾을 수 없습니다."),

    // 지원 현황
    APPLICATION_NOT_FOUND(404, "지원 정보를 찾을 수 없습니다."),
    HISTORY_NOT_FOUND(404, "전형 단계를 찾을 수 없습니다."),

    // 공통
    INVALID_REQUEST(400, "잘못된 요청입니다."),

    // 외부 API
    KAKAO_AUTH_FAILED(502, "카카오 인증에 실패했습니다."),
    ALARM_SEND_FAILED(502, "알림톡 발송에 실패했습니다.");

    private final int status;
    private final String message;
}
