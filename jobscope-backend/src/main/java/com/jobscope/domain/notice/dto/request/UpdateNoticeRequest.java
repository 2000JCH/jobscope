package com.jobscope.domain.notice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateNoticeRequest {

    @Size(max = 200)
    private String title;

    private String content;

    private Boolean active;
}