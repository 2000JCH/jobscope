package com.jobscope.domain.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StageDurationResponse {

    private String fromStage;
    private String toStage;
    private double avgDays;
    private int sampleCount;
}