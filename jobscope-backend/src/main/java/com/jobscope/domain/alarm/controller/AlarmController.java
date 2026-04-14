package com.jobscope.domain.alarm.controller;

import com.jobscope.domain.alarm.dto.response.AlarmLogResponse;
import com.jobscope.domain.alarm.service.AlarmService;
import com.jobscope.global.common.response.ApiResponse;
import com.jobscope.global.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AlarmLogResponse>>> getAlarmLogs(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(alarmService.findAlarmLogs(userId, page, size)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAlarmLogs(
            @AuthenticationPrincipal Long userId,
            @RequestBody List<Long> ids) {
        alarmService.deleteAlarmLogs(userId, ids);
        return ResponseEntity.ok(ApiResponse.success());
    }
}