package com.jobscope.domain.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrustMetricsResponse {

    private int totalCount;
    private int noHistoryCount;
    private int inProgressCount;
}