package com.jobscope.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyRankResponse {

    private String companyName;
    private long count;
}