package com.jobscope.domain.application.controller;

import com.jobscope.domain.application.dto.request.CreateHistoryRequest;
import com.jobscope.domain.application.dto.request.UpdateHistoryRequest;
import com.jobscope.domain.application.service.ApplicationHistoryService;
import com.jobscope.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/applications/{applicationId}/histories")
@RequiredArgsConstructor
public class ApplicationHistoryController {

    private final ApplicationHistoryService applicationHistoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> createHistory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateHistoryRequest request) {
        Long id = applicationHistoryService.createHistory(applicationId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("id", id)));
    }

    @PatchMapping("/{historyId}")
    public ResponseEntity<ApiResponse<Void>> updateHistory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long applicationId,
            @PathVariable Long historyId,
            @Valid @RequestBody UpdateHistoryRequest request) {
        applicationHistoryService.updateHistory(applicationId, historyId, userId, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{historyId}")
    public ResponseEntity<ApiResponse<Void>> deleteHistory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long applicationId,
            @PathVariable Long historyId) {
        applicationHistoryService.deleteHistory(applicationId, historyId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}