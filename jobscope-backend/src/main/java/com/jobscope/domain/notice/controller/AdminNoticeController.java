package com.jobscope.domain.notice.controller;

import com.jobscope.domain.notice.dto.request.CreateNoticeRequest;
import com.jobscope.domain.notice.dto.request.UpdateNoticeRequest;
import com.jobscope.domain.notice.dto.response.NoticeResponse;
import com.jobscope.domain.notice.service.NoticeService;
import com.jobscope.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoticeResponse>>> getNotices() {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getAllNotices()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(
            @RequestBody @Valid CreateNoticeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(noticeService.createNotice(request)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateNotice(
            @PathVariable Long id,
            @RequestBody @Valid UpdateNoticeRequest request) {
        noticeService.updateNotice(id, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}