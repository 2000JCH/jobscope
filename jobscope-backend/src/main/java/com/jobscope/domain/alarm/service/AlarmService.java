package com.jobscope.domain.alarm.service;

import com.jobscope.domain.alarm.dto.response.AlarmLogResponse;
import com.jobscope.domain.alarm.entity.AlarmLog;
import com.jobscope.domain.alarm.entity.AlarmType;
import com.jobscope.domain.alarm.repository.AlarmLogRepository;
import com.jobscope.domain.application.service.ApplicationService;
import com.jobscope.global.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AlarmService {

    private final AlarmLogRepository alarmLogRepository;
    private final ApplicationService applicationService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * 내 알림 발송 이력을 최신순으로 페이지 조회한다.
     *
     * @param userId 인증된 사용자 ID
     * @param page   페이지 번호
     * @param size   페이지 크기
     * @return 알림 발송 이력 페이지 응답
     */
    public PageResponse<AlarmLogResponse> findAlarmLogs(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AlarmLog> logPage = alarmLogRepository.findByUserIdOrderBySentAtDesc(userId, pageable);

        List<Long> applicationIds = logPage.getContent().stream()
                .map(AlarmLog::getApplicationId).distinct().toList();
        Map<Long, String> companyNameMap = applicationService.findCompanyNamesByIdsForAlarmHistory(applicationIds);

        List<Long> historyIds = logPage.getContent().stream()
                .map(AlarmLog::getApplicationHistoryId)
                .filter(Objects::nonNull).distinct().toList();
        Map<Long, String> stageMap = applicationService.findStagesByHistoryIdsForAlarmHistory(historyIds);

        return PageResponse.from(logPage.map(alarmLog -> {
            String companyName = companyNameMap.getOrDefault(alarmLog.getApplicationId(), "(삭제된 지원)");
            String historyStage = alarmLog.getApplicationHistoryId() != null
                    ? stageMap.get(alarmLog.getApplicationHistoryId()) : null;
            return AlarmLogResponse.of(alarmLog, companyName, historyStage);
        }));
    }

    /**
     * 알림 발송 결과를 AlarmLog에 기록한다.
     * 중복 발송 방지를 위해 DB 유니크 제약으로 원천 차단하며,
     * DuplicateKeyException은 호출 측(AlarmScheduler)에서 catch한다.
     *
     * @param userId               수신 유저 ID
     * @param applicationId        대상 지원 ID
     * @param applicationHistoryId 대상 히스토리 ID (DEADLINE 타입이면 null)
     * @param alarmType            알림 타입
     * @param isSuccess            발송 성공 여부
     */
    /**
     * 선택한 알림 이력을 삭제한다. 본인 소유 이력만 삭제된다.
     *
     * @param userId 인증된 사용자 ID
     * @param ids    삭제할 알림 이력 ID 목록
     */
    @Transactional
    public void deleteAlarmLogs(Long userId, List<Long> ids) {
        alarmLogRepository.deleteByIdInAndUserId(ids, userId);
        log.info("[AlarmService] 알림 이력 삭제 완료 - userId: {}, count: {}", userId, ids.size());
    }

    // NOTE: REQUIRES_NEW — 각 알림 로그가 독립적인 트랜잭션으로 처리됨
    // 한 건의 DuplicateKeyException이 다른 알림 로그 트랜잭션에 영향을 주지 않도록 격리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAlarm(Long userId, Long applicationId, Long applicationHistoryId,
                         AlarmType alarmType, boolean isSuccess) {
        LocalDateTime sentAt = LocalDateTime.now(KST);
        AlarmLog alarmLog = AlarmLog.builder()
                .userId(userId)
                .applicationId(applicationId)
                .applicationHistoryId(applicationHistoryId)
                .alarmType(alarmType)
                .sentAt(sentAt)
                .isSuccess(isSuccess)
                .build();
        alarmLogRepository.save(alarmLog);

        if (isSuccess) {
            log.info("[AlarmService] 알림 로그 저장 완료 - userId: {}, applicationId: {}, alarmType: {}",
                    userId, applicationId, alarmType);
        } else {
            log.warn("[AlarmService] 알림 발송 실패 로그 저장 - userId: {}, applicationId: {}, alarmType: {}",
                    userId, applicationId, alarmType);
        }
    }
}