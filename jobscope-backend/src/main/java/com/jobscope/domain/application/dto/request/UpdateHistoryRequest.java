package com.jobscope.domain.application.dto.request;

import com.jobscope.domain.application.entity.StageResult;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UpdateHistoryRequest {

    private String stage;
    private StageResult stageResult;
    private LocalDateTime scheduledAt;
    private LocalDateTime completedAt;
    private String memo;
}