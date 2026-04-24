package com.jobscope.domain.notice.controller;

import com.jobscope.domain.notice.dto.response.NoticeResponse;
import com.jobscope.domain.notice.service.NoticeService;
import com.jobscope.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<NoticeResponse>> getLatestNotice() {
        return ResponseEntity.ok(ApiResponse.success(
                noticeService.getLatestActiveNotice().orElse(null)
        ));
    }
}