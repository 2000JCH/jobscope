package com.jobscope.domain.application.entity;

import com.jobscope.global.common.entity.SoftDeleteEntity;
import com.jobscope.global.common.exception.BusinessException;
import com.jobscope.global.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "application")
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder
public class Application extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String jobPosition;

    @Column(nullable = false)
    private LocalDate appliedAt;

    private LocalDateTime deadlineAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationResult result = ApplicationResult.IN_PROGRESS;

    @Column(nullable = false)
    @Builder.Default
    private boolean alarmEnabled = true;

    private String jobPostingUrl;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(columnDefinition = "TEXT")
    private String retrospective;

    public void validateApplicationOwner(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    public Long calculateDDay(LocalDate today) {
        if (deadlineAt == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(today, deadlineAt.toLocalDate());
    }

    public void updateBasicInfo(String companyName, String jobPosition, LocalDate appliedAt,
                                LocalDateTime deadlineAt, String jobPostingUrl, String memo) {
        if (companyName != null) { this.companyName = companyName; }
        if (jobPosition != null) { this.jobPosition = jobPosition; }
        if (appliedAt != null) { this.appliedAt = appliedAt; }
        if (deadlineAt != null) { this.deadlineAt = deadlineAt; }
        if (jobPostingUrl != null) { this.jobPostingUrl = jobPostingUrl; }
        if (memo != null) { this.memo = memo; }
    }

    public void updateAlarmEnabled(boolean alarmEnabled) {
        this.alarmEnabled = alarmEnabled;
    }

    public void updateResult(ApplicationResult result) {
        this.result = result;
    }

    public void updateRetrospective(String retrospective) {
        if (this.result != ApplicationResult.FAILED) {
            return;
        }
        this.retrospective = retrospective;
    }
}