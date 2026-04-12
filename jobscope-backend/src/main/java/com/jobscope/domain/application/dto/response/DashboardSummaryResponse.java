package com.jobscope.domain.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardSummaryResponse {

    private long total;
    private long inProgress;
    private long passed;
    private long failed;
}