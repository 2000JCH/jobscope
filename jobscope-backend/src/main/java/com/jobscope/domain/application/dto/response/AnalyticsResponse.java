package com.jobscope.domain.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AnalyticsResponse {

    private TrustMetricsResponse trustMetrics;
    private List<FunnelStageResponse> funnelStages;
    private List<StageDurationResponse> stageDurations;

    public static AnalyticsResponse empty() {
        return AnalyticsResponse.builder()
                .trustMetrics(TrustMetricsResponse.builder()
                        .totalCount(0).noHistoryCount(0).inProgressCount(0).build())
                .funnelStages(List.of())
                .stageDurations(List.of())
                .build();
    }
}