package com.jobscope.domain.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class CreateApplicationRequest {

    @NotBlank
    private String companyName;

    @NotBlank
    private String jobPosition;

    @NotNull
    private LocalDate appliedAt;

    private LocalDateTime deadlineAt;

    private Boolean alarmEnabled;

    private String jobPostingUrl;

    private String memo;
}