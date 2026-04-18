package com.jobscope.global.scheduler;

import com.jobscope.domain.alarm.entity.AlarmType;
import com.jobscope.domain.alarm.service.AlarmService;
import com.jobscope.domain.application.entity.Application;
import com.jobscope.domain.application.entity.ApplicationHistory;
import com.jobscope.domain.application.service.ApplicationService;
import com.jobscope.global.alarm.KakaoMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlarmScheduler {

    private final AlarmService alarmService;
    private final ApplicationService applicationService;
    private final KakaoMessageService kakaoMessageService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * 매일 오전 9시(KST)에 실행된다.
     * 같은 alarmType끼리 유저별로 그루핑하여 1건의 카카오 메시지로 발송한다.
     * - DEADLINE 알림: 서류 마감일이 오늘인 지원건
     * - D1/D3/D7 알림: 전형 예정일이 내일/3일 후/7일 후인 히스토리
     */
    //@Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void runDailyAlarms() {
        LocalDate today = LocalDate.now(KST);
        log.info("[AlarmScheduler] 일일 알림 발송 시작 - date: {}", today);

        sendGroupedDeadlineAlarms(today);
        sendGroupedScheduledAlarms(today.plusDays(1), AlarmType.D1);
        sendGroupedScheduledAlarms(today.plusDays(3), AlarmType.D3);
        sendGroupedScheduledAlarms(today.plusDays(7), AlarmType.D7);

        log.info("[AlarmScheduler] 일일 알림 발송 완료 - date: {}", today);
    }

    // NOTE: AlarmScheduler 쿼리 조건 — deleted_at IS NULL + alarm_enabled = TRUE
    // ApplicationRepository.findAlarmDeadlineTargets()에서 JPQL로 자동 적용됨
    private void sendGroupedDeadlineAlarms(LocalDate today) {
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);

        List<Application> targets = applicationService.findDeadlineAlarmTargetsForScheduler(start, end);
        if (targets.isEmpty()) {
            return;
        }

        // userId별로 그루핑
        Map<Long, List<Application>> byUser = targets.stream()
                .collect(Collectors.groupingBy(Application::getUserId));

        for (Map.Entry<Long, List<Application>> entry : byUser.entrySet()) {
            Long userId = entry.getKey();
            List<Application> apps = entry.getValue();

            // DEADLINE은 전부 stage = "서류 마감"
            Map<String, List<String>> stageToCompanies = new LinkedHashMap<>();
            stageToCompanies.put("서류 마감",
                    apps.stream().map(Application::getCompanyName).toList());

            boolean isSuccess = kakaoMessageService.sendMessage(
                    userId, AlarmType.DEADLINE, apps.size(), stageToCompanies);

            for (Application app : apps) {
                logAlarmSafely(userId, app.getId(), null, AlarmType.DEADLINE, isSuccess);
            }
        }
    }

    // NOTE: AlarmScheduler 쿼리 조건 — deleted_at IS NULL + alarm_enabled = TRUE
    // ApplicationHistoryRepository.findScheduledAlarmTargets()에서 JPQL JOIN FETCH로 자동 적용됨
    private void sendGroupedScheduledAlarms(LocalDate targetDate, AlarmType alarmType) {
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.atTime(23, 59, 59);

        List<ApplicationHistory> targets = applicationService.findScheduledAlarmTargetsForScheduler(start, end);
        if (targets.isEmpty()) {
            return;
        }

        // userId별로 그루핑
        Map<Long, List<ApplicationHistory>> byUser = targets.stream()
                .collect(Collectors.groupingBy(h -> h.getApplication().getUserId()));

        for (Map.Entry<Long, List<ApplicationHistory>> entry : byUser.entrySet()) {
            Long userId = entry.getKey();
            List<ApplicationHistory> histories = entry.getValue();

            // 같은 userId 내에서 stage별로 회사명 묶기 (삽입 순서 유지)
            Map<String, List<String>> stageToCompanies = new LinkedHashMap<>();
            for (ApplicationHistory history : histories) {
                stageToCompanies
                        .computeIfAbsent(history.getStage(), k -> new ArrayList<>())
                        .add(history.getApplication().getCompanyName());
            }

            boolean isSuccess = kakaoMessageService.sendMessage(
                    userId, alarmType, histories.size(), stageToCompanies);

            for (ApplicationHistory history : histories) {
                logAlarmSafely(userId, history.getApplication().getId(),
                        history.getId(), alarmType, isSuccess);
            }
        }
    }

    /**
     * 알림 로그를 기록하고, 중복 발송 시도(DuplicateKeyException)는 WARN 로그만 남기고 정상 흐름 처리한다.
     * AlarmService.logAlarm()이 @Transactional(REQUIRES_NEW)이므로 각 로그는 독립적인 트랜잭션으로 처리된다.
     *
     * NOTE: 발송 실패 시에도 isSuccess=false로 로그를 저장한다 (발송 이력 보존 목적).
     * uq_alarm_prevent의 유니크 키는 DATE(sent_at)을 포함하므로, 다음 날 스케줄러 실행 시
     * 날짜가 달라 새 레코드 삽입이 가능하다. 단, DEADLINE/D1/D3/D7 알림 특성상
     * 다음 날에는 해당 건이 발송 대상에서 빠지므로 재발송 없이 실패 이력만 남는다.
     */
    private void logAlarmSafely(Long userId, Long applicationId, Long historyId,
                                AlarmType alarmType, boolean isSuccess) {
        try {
            alarmService.logAlarm(userId, applicationId, historyId, alarmType, isSuccess);
        } catch (DuplicateKeyException e) {
            // NOTE: DB 유니크 제약(uq_alarm_prevent)으로 중복 발송 원천 차단 — 정상 흐름
            log.warn("[AlarmScheduler] 중복 발송 감지 — 건너뜀 - userId: {}, applicationId: {}, alarmType: {}",
                    userId, applicationId, alarmType);
        }
    }
}