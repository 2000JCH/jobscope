package com.jobscope.domain.application.dto.response;

import com.jobscope.domain.application.entity.Application;
import com.jobscope.domain.application.entity.ApplicationResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ApplicationDetailResponse {

    private Long id;
    private String companyName;
    private String jobPosition;
    private String appliedAt;
    private String deadlineAt;
    private Long dDay;
    private ApplicationResult result;
    private boolean alarmEnabled;
    private String jobPostingUrl;
    private String memo;
    private String retrospective;
    private List<ApplicationHistoryResponse> histories;

    public static ApplicationDetailResponse of(Application application, Long dDay,
                                               List<ApplicationHistoryResponse> histories) {
        return ApplicationDetailResponse.builder()
                .id(application.getId())
                .companyName(application.getCompanyName())
                .jobPosition(application.getJobPosition())
                .appliedAt(application.getAppliedAt().toString())
                .deadlineAt(application.getDeadlineAt() != null ? application.getDeadlineAt().toString() : null)
                .dDay(dDay)
                .result(application.getResult())
                .alarmEnabled(application.isAlarmEnabled())
                .jobPostingUrl(application.getJobPostingUrl())
                .memo(application.getMemo())
                .retrospective(application.getRetrospective())
                .histories(histories)
                .build();
    }
}