package com.jobscope.domain.application.controller;

import com.jobscope.domain.application.dto.request.CreateApplicationRequest;
import com.jobscope.domain.application.dto.request.UpdateApplicationRequest;
import com.jobscope.domain.application.dto.response.ApplicationDetailResponse;
import com.jobscope.domain.application.dto.response.ApplicationSummaryResponse;
import com.jobscope.domain.application.dto.response.CalendarDateResponse;
import com.jobscope.domain.application.dto.response.DashboardResponse;
import com.jobscope.domain.application.entity.ApplicationResult;
import com.jobscope.domain.application.entity.ApplicationSort;
import com.jobscope.domain.application.service.ApplicationService;
import com.jobscope.global.common.response.ApiResponse;
import com.jobscope.global.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ApplicationSummaryResponse>>> getApplications(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<ApplicationResult> result,
            @RequestParam(required = false) ApplicationSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.findApplications(userId, search, result, sort, page, size)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> createApplication(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateApplicationRequest request) {
        Long id = applicationService.createApplication(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("id", id)));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getDashboard(userId)));
    }

    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<List<CalendarDateResponse>>> getCalendar(
            @AuthenticationPrincipal Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.getCalendar(userId, startDate, endDate)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationDetailResponse>> getApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                applicationService.findApplicationDetail(id, userId)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateApplicationRequest request) {
        applicationService.updateApplication(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        applicationService.deleteApplication(id, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}