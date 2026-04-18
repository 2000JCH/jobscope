package com.jobscope.domain.application.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class CalendarDateResponse {

    private LocalDate date;
    private List<CalendarEventResponse> events;
}