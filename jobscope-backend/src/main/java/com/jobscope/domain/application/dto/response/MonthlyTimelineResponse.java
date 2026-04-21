package com.jobscope.domain.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyTimelineResponse {

    private String yearMonth;
    private int appliedCount;
    private int interviewCount;
    private int passedCount;
    private int failedCount;
}