package com.jobscope.domain.application.controller;

import com.jobscope.domain.application.dto.response.AnalyticsResponse;
import com.jobscope.domain.application.service.AnalyticsService;
import com.jobscope.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String jobPosition,
            @RequestParam(required = false) Boolean hasCodeTest) {
        return ResponseEntity.ok(ApiResponse.success(
                analyticsService.getAnalytics(userId, jobPosition, hasCodeTest)));
    }
}