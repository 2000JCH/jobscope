package com.jobscope.domain.application.dto.response;

import com.jobscope.domain.application.entity.ApplicationHistory;
import com.jobscope.domain.application.entity.StageResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationHistoryResponse {

    private Long id;
    private String stage;
    private StageResult stageResult;
    private String scheduledAt;
    private String completedAt;
    private String memo;

    public static ApplicationHistoryResponse from(ApplicationHistory history) {
        return ApplicationHistoryResponse.builder()
                .id(history.getId())
                .stage(history.getStage())
                .stageResult(history.getStageResult())
                .scheduledAt(history.getScheduledAt() != null ? history.getScheduledAt().toString() : null)
                .completedAt(history.getCompletedAt() != null ? history.getCompletedAt().toString() : null)
                .memo(history.getMemo())
                .build();
    }
}