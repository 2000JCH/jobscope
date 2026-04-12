package com.jobscope.domain.application.service;

import com.jobscope.domain.application.dto.request.CreateHistoryRequest;
import com.jobscope.domain.application.dto.request.UpdateHistoryRequest;
import com.jobscope.domain.application.entity.Application;
import com.jobscope.domain.application.entity.ApplicationHistory;
import com.jobscope.domain.application.entity.ApplicationResult;
import com.jobscope.domain.application.entity.StageResult;
import com.jobscope.domain.application.repository.ApplicationHistoryRepository;
import com.jobscope.domain.application.repository.ApplicationRepository;
import com.jobscope.global.common.exception.BusinessException;
import com.jobscope.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ApplicationHistoryService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private Application findAndValidateApplication(Long applicationId, Long userId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));
        application.validateApplicationOwner(userId);
        return application;
    }

    private ApplicationHistory findAndValidateHistory(Long applicationId, Long historyId, Long userId) {
        findAndValidateApplication(applicationId, userId);
        ApplicationHistory history = applicationHistoryRepository.findById(historyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HISTORY_NOT_FOUND));
        history.validateBelongsTo(applicationId);
        return history;
    }

    /**
     * 전형 단계를 등록한다.
     *
     * @param applicationId Application PK
     * @param userId        인증된 사용자 ID
     * @param request       전형 단계 등록 요청 DTO
     * @return 생성된 ApplicationHistory PK
     * @throws BusinessException 지원 없음(APPLICATION_NOT_FOUND) 또는 타인 리소스(FORBIDDEN)
     */
    @Transactional
    public Long createHistory(Long applicationId, Long userId, CreateHistoryRequest request) {
        Application application = findAndValidateApplication(applicationId, userId);

        ApplicationHistory history = ApplicationHistory.builder()
                .application(application)
                .stage(request.getStage())
                .stageResult(request.getStageResult() != null ? request.getStageResult() : StageResult.PENDING)
                .scheduledAt(request.getScheduledAt())
                .memo(request.getMemo())
                .build();
        applicationHistoryRepository.save(history);

        log.info("[ApplicationHistoryService] 히스토리 등록 완료 - applicationId: {}, historyId: {}",
                applicationId, history.getId());
        return history.getId();
    }

    /**
     * 전형 단계를 수정한다. null 필드는 변경하지 않는다.
     * stageResult가 PASS/FAIL로 변경되고 해당 히스토리가 최신 단계이면 APPLICATION.result도 동시 업데이트한다.
     *
     * @param applicationId Application PK
     * @param historyId     ApplicationHistory PK
     * @param userId        인증된 사용자 ID
     * @param request       수정 요청 DTO
     * @throws BusinessException 지원/히스토리 없음 또는 타인 리소스(FORBIDDEN)
     */
    @Transactional
    public void updateHistory(Long applicationId, Long historyId, Long userId, UpdateHistoryRequest request) {
        ApplicationHistory history = findAndValidateHistory(applicationId, historyId, userId);
        LocalDateTime now = LocalDateTime.now(KST);

        history.update(request.getStage(), request.getStageResult(),
                request.getScheduledAt(), request.getCompletedAt(), request.getMemo(), now);

        // NOTE: id 최댓값 기준으로 최신 히스토리 판별 (created_at 사용 금지)
        if (request.getStageResult() != null && request.getStageResult() != StageResult.PENDING) {
            applicationHistoryRepository.findTopByApplicationIdOrderByIdDesc(applicationId)
                    .filter(latest -> latest.getId().equals(historyId))
                    .ifPresent(latest -> {
                        ApplicationResult appResult = request.getStageResult().toApplicationResult();
                        if (appResult != null) {
                            history.getApplication().updateResult(appResult);
                        }
                    });
        }

        log.info("[ApplicationHistoryService] 히스토리 수정 완료 - applicationId: {}, historyId: {}",
                applicationId, historyId);
    }

    /**
     * 전형 단계를 삭제한다 (Hard Delete).
     *
     * @param applicationId Application PK
     * @param historyId     ApplicationHistory PK
     * @param userId        인증된 사용자 ID
     * @throws BusinessException 지원/히스토리 없음 또는 타인 리소스(FORBIDDEN)
     */
    @Transactional
    public void deleteHistory(Long applicationId, Long historyId, Long userId) {
        ApplicationHistory history = findAndValidateHistory(applicationId, historyId, userId);
        applicationHistoryRepository.delete(history);
        log.info("[ApplicationHistoryService] 히스토리 삭제 완료 - applicationId: {}, historyId: {}",
                applicationId, historyId);
    }
}