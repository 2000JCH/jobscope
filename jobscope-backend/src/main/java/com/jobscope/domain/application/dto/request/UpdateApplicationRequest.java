package com.jobscope.domain.application.dto.request;

import com.jobscope.domain.application.entity.ApplicationResult;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class UpdateApplicationRequest {

    private String companyName;
    private String jobPosition;
    private LocalDate appliedAt;
    private LocalDateTime deadlineAt;
    private Boolean alarmEnabled;
    private String jobPostingUrl;
    private String memo;
    private ApplicationResult result;
    private String retrospective;
}