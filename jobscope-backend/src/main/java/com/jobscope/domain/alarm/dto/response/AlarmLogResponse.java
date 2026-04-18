package com.jobscope.domain.alarm.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jobscope.domain.alarm.entity.AlarmLog;
import com.jobscope.domain.alarm.entity.AlarmType;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class AlarmLogResponse {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private Long id;
    private String companyName;
    private String alarmType;
    private String label;
    private String sentAt;

    // NOTE: boxed Boolean 사용 → Jackson이 getIsSuccess()를 isSuccess로 직렬화
    @JsonProperty("isSuccess")
    private Boolean isSuccess;

    /**
     * AlarmLog 엔티티와 조회된 부가 정보로 응답 DTO를 생성한다.
     *
     * @param log          AlarmLog 엔티티
     * @param companyName  Application의 회사명 (소프트 삭제된 경우 "(삭제된 지원)")
     * @param historyStage D7/D3/D1 타입의 전형 단계명 (DEADLINE 타입은 null)
     * @return AlarmLogResponse
     */
    public static AlarmLogResponse of(AlarmLog log, String companyName, String historyStage) {
        // NOTE: DEADLINE 타입은 서류 마감 고정, D7/D3/D1은 연결된 히스토리의 stage 반환
        String label = log.getAlarmType() == AlarmType.DEADLINE
                ? "서류 마감"
                : (historyStage != null ? historyStage : "알 수 없음");

        return AlarmLogResponse.builder()
                .id(log.getId())
                .companyName(companyName)
                .alarmType(log.getAlarmType().name())
                .label(label)
                .sentAt(log.getSentAt().format(FORMATTER))
                .isSuccess(log.isSuccess())
                .build();
    }
}