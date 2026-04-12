package com.jobscope.domain.application.entity;

import com.jobscope.global.common.entity.BaseEntity;
import com.jobscope.global.common.exception.BusinessException;
import com.jobscope.global.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "application_history")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder
public class ApplicationHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(nullable = false)
    private String stage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StageResult stageResult = StageResult.PENDING;

    private LocalDateTime scheduledAt;
    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String memo;

    public static ApplicationHistory createInitial(Application application) {
        return ApplicationHistory.builder()
                .application(application)
                .stage("서류")
                .stageResult(StageResult.PENDING)
                .build();
    }

    public void validateBelongsTo(Long applicationId) {
        if (!this.application.getId().equals(applicationId)) {
            throw new BusinessException(ErrorCode.HISTORY_NOT_FOUND);
        }
    }

    public void update(String stage, StageResult stageResult, LocalDateTime scheduledAt,
                       LocalDateTime completedAt, String memo, LocalDateTime now) {
        if (stage != null) { this.stage = stage; }
        if (scheduledAt != null) { this.scheduledAt = scheduledAt; }
        if (memo != null) { this.memo = memo; }

        if (stageResult != null) { this.stageResult = stageResult; }

        // completedAt 처리 우선순위
        // 1순위: 요청에 completedAt 값이 있으면 그 값 사용
        if (completedAt != null) {
            this.completedAt = completedAt;
        } else if (stageResult == StageResult.PASS || stageResult == StageResult.FAIL) {
            // 2순위: completedAt 없고 stageResult가 PASS/FAIL이면 현재 시각 자동 기록
            this.completedAt = now;
        }
        // 3순위: stageResult 변경 없으면 completedAt 그대로 유지
    }

    public void updateStageResultWithCompletion(StageResult stageResult, LocalDateTime now) {
        this.stageResult = stageResult;
        this.completedAt = now;
    }
}