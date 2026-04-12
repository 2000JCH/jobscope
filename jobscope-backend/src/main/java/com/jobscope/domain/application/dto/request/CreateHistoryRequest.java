package com.jobscope.domain.application.dto.request;

import com.jobscope.domain.application.entity.StageResult;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateHistoryRequest {

    @NotBlank
    private String stage;

    private StageResult stageResult;

    private LocalDateTime scheduledAt;

    private String memo;
}