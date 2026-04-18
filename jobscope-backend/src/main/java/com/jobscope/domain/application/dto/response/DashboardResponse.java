package com.jobscope.domain.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    private DashboardSummaryResponse summary;
    private List<DashboardScheduleResponse> thisWeekSchedules;
    private List<DashboardDeadlineResponse> imminentDeadlines;
}