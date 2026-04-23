package com.jobscope.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyStatResponse {

    private String date;
    private long count;
}