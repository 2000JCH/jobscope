package com.jobscope.domain.application.dto.response;

import com.jobscope.domain.application.entity.Application;
import com.jobscope.domain.application.entity.ApplicationResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationSummaryResponse {

    private Long id;
    private String companyName;
    private String jobPosition;
    private String appliedAt;
    private String deadlineAt;
    private ApplicationResult result;
    private String currentStage;
    private boolean alarmEnabled;
    private Long dDay;

    public static ApplicationSummaryResponse of(Application application, String currentStage, Long dDay) {
        return ApplicationSummaryResponse.builder()
                .id(application.getId())
                .companyName(application.getCompanyName())
                .jobPosition(application.getJobPosition())
                .appliedAt(application.getAppliedAt().toString())
                .deadlineAt(application.getDeadlineAt() != null ? application.getDeadlineAt().toString() : null)
                .result(application.getResult())
                .currentStage(currentStage)
                .alarmEnabled(application.isAlarmEnabled())
                .dDay(dDay)
                .build();
    }
}