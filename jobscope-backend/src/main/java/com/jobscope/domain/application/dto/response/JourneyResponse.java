package com.jobscope.domain.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class JourneyResponse {

    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private long journeyDays;
    private int totalCount;
    private int passedCount;
    private int failedCount;
    private int inProgressCount;
    private List<FunnelStageResponse> funnelStages;
    private List<MonthlyTimelineResponse> monthlyTimeline;
}