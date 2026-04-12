package com.jobscope.domain.application.dto.response;

import com.jobscope.domain.application.entity.CalendarEventType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CalendarEventResponse {

    private CalendarEventType type;
    private Long applicationId;
    private String companyName;
    private String label;
    private String time;
}