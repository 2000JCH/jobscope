package com.jobscope.domain.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DashboardScheduleResponse {

    private Long applicationId;
    private String companyName;
    private String stage;
    private LocalDateTime scheduledAt;
}