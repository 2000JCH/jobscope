package com.jobscope.domain.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DashboardDeadlineResponse {

    private Long applicationId;
    private String companyName;
    private LocalDateTime deadlineAt;
    private Long dDay;
}