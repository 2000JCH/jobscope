package com.jobscope.domain.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FunnelStageResponse {

    private String stageName;
    private int passCount;
    private int failCount;
    private int pendingCount;
    private Double passRate;
    private boolean maxDrop;
}