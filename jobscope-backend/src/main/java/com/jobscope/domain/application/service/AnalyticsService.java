package com.jobscope.domain.application.service;

import com.jobscope.domain.application.dto.response.AnalyticsResponse;
import com.jobscope.domain.application.dto.response.FunnelStageResponse;
import com.jobscope.domain.application.dto.response.JourneyResponse;
import com.jobscope.domain.application.dto.response.MonthlyTimelineResponse;
import com.jobscope.domain.application.dto.response.StageDurationResponse;
import com.jobscope.domain.application.dto.response.TrustMetricsResponse;
import com.jobscope.domain.application.entity.Application;
import com.jobscope.domain.application.entity.ApplicationHistory;
import com.jobscope.domain.application.entity.ApplicationResult;
import com.jobscope.domain.application.entity.StageResult;
import com.jobscope.domain.application.repository.ApplicationHistoryRepository;
import com.jobscope.domain.application.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AnalyticsService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Set<String> CODE_TEST_KEYWORDS = Set.of("코딩테스트", "코테");

    /**
     * 분석 데이터를 조회한다. 직군 필터, 코딩테스트 유무 필터를 지원한다.
     *
     * @param userId      인증된 사용자 ID
     * @param jobPosition 직군 필터 (null이면 전체)
     * @param hasCodeTest 코딩테스트 유무 필터 (null이면 전체)
     * @return 신뢰 지표, 단계별 퍼널, 구간별 소요 기간
     */
    public AnalyticsResponse getAnalytics(Long userId, String jobPosition, Boolean hasCodeTest) {
        List<Application> apps = applicationRepository.findByUserId(userId);

        if (jobPosition != null && !jobPosition.isBlank()) {
            apps = apps.stream()
                    .filter(a -> a.getJobPosition().contains(jobPosition))
                    .toList();
        }

        if (apps.isEmpty()) {
            return AnalyticsResponse.empty();
        }

        List<Long> appIds = apps.stream().map(Application::getId).toList();
        List<ApplicationHistory> allHistories = applicationHistoryRepository.findByApplicationIdIn(appIds);

        Map<Long, List<ApplicationHistory>> historiesByAppId = allHistories.stream()
                .collect(Collectors.groupingBy(h -> h.getApplication().getId()));

        Set<Long> noHistoryAppIds = apps.stream()
                .filter(a -> isNoHistory(historiesByAppId.getOrDefault(a.getId(), List.of())))
                .map(Application::getId)
                .collect(Collectors.toSet());

        if (hasCodeTest != null) {
            Set<Long> codeTestAppIds = allHistories.stream()
                    .filter(h -> isCodeTestStage(h.getStage()))
                    .map(h -> h.getApplication().getId())
                    .collect(Collectors.toSet());
            apps = apps.stream()
                    .filter(a -> hasCodeTest.equals(codeTestAppIds.contains(a.getId())))
                    .toList();
        }

        int totalCount = apps.size();
        Set<Long> finalAppIds = apps.stream().map(Application::getId).collect(Collectors.toSet());
        int noHistoryCount = (int) finalAppIds.stream().filter(noHistoryAppIds::contains).count();
        int inProgressCount = (int) apps.stream()
                .filter(a -> a.getResult() == ApplicationResult.IN_PROGRESS).count();

        TrustMetricsResponse trustMetrics = TrustMetricsResponse.builder()
                .totalCount(totalCount)
                .noHistoryCount(noHistoryCount)
                .inProgressCount(inProgressCount)
                .build();

        List<ApplicationHistory> analyticsHistories = allHistories.stream()
                .filter(h -> finalAppIds.contains(h.getApplication().getId()))
                .filter(h -> !noHistoryAppIds.contains(h.getApplication().getId()))
                .sorted(Comparator.comparingLong(ApplicationHistory::getId))
                .toList();

        List<FunnelStageResponse> funnelStages = buildFunnel(analyticsHistories);

        // analyticsHistories는 이미 id 오름차순 정렬됨 — groupingBy가 순서 보존
        Map<Long, List<ApplicationHistory>> sortedHistoriesByApp = analyticsHistories.stream()
                .collect(Collectors.groupingBy(h -> h.getApplication().getId()));

        List<StageDurationResponse> stageDurations = buildStageDurations(sortedHistoriesByApp);

        log.info("[AnalyticsService] 분석 데이터 조회 완료 - userId: {}, totalCount: {}", userId, totalCount);

        return AnalyticsResponse.builder()
                .trustMetrics(trustMetrics)
                .funnelStages(funnelStages)
                .stageDurations(stageDurations)
                .build();
    }

    /**
     * MY 탭 취준 여정 데이터를 조회한다.
     * 첫 지원일·기간·퍼널·월별 타임라인을 포함한다.
     *
     * @param userId 인증된 사용자 ID
     * @return 취준 여정 응답 (지원 없으면 빈 응답)
     */
    public JourneyResponse getJourney(Long userId) {
        List<Application> apps = applicationRepository.findByUserId(userId);

        if (apps.isEmpty()) {
            LocalDate today = LocalDate.now(KST);
            return JourneyResponse.builder()
                    .status("IN_PROGRESS")
                    .startDate(today)
                    .endDate(today)
                    .journeyDays(0)
                    .totalCount(0)
                    .passedCount(0)
                    .failedCount(0)
                    .inProgressCount(0)
                    .funnelStages(List.of())
                    .monthlyTimeline(List.of())
                    .build();
        }

        List<Long> appIds = apps.stream().map(Application::getId).toList();
        List<ApplicationHistory> allHistories = applicationHistoryRepository.findByApplicationIdIn(appIds);

        Map<Long, List<ApplicationHistory>> historiesByAppId = allHistories.stream()
                .collect(Collectors.groupingBy(h -> h.getApplication().getId()));

        int totalCount = apps.size();
        int passedCount = (int) apps.stream().filter(a -> a.getResult() == ApplicationResult.PASSED).count();
        int failedCount = (int) apps.stream().filter(a -> a.getResult() == ApplicationResult.FAILED).count();
        int inProgressCount = (int) apps.stream().filter(a -> a.getResult() == ApplicationResult.IN_PROGRESS).count();

        LocalDate today = LocalDate.now(KST);

        LocalDate startDate = apps.stream()
                .map(Application::getAppliedAt)
                .min(Comparator.naturalOrder())
                .orElse(today);

        String status;
        LocalDate endDate;

        if (passedCount > 0) {
            status = "PASSED";
            // NOTE: 각 PASSED 지원건별 id 최댓값 히스토리의 completedAt을 구한 뒤 그 중 최댓값 선택
            endDate = apps.stream()
                    .filter(a -> a.getResult() == ApplicationResult.PASSED)
                    .map(a -> historiesByAppId.getOrDefault(a.getId(), List.of()).stream()
                            .max(Comparator.comparingLong(ApplicationHistory::getId))
                            .map(ApplicationHistory::getCompletedAt)
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .map(LocalDateTime::toLocalDate)
                    .max(Comparator.naturalOrder())
                    .orElse(today);
        } else {
            status = "IN_PROGRESS";
            endDate = today;
        }

        long journeyDays = Math.max(1, ChronoUnit.DAYS.between(startDate, endDate) + 1);

        Set<Long> noHistoryAppIds = apps.stream()
                .filter(a -> isNoHistory(historiesByAppId.getOrDefault(a.getId(), List.of())))
                .map(Application::getId)
                .collect(Collectors.toSet());

        List<ApplicationHistory> analyticsHistories = allHistories.stream()
                .filter(h -> !noHistoryAppIds.contains(h.getApplication().getId()))
                .sorted(Comparator.comparingLong(ApplicationHistory::getId))
                .toList();

        List<FunnelStageResponse> funnelStages = buildFunnel(analyticsHistories);
        List<MonthlyTimelineResponse> monthlyTimeline = buildMonthlyTimeline(apps, allHistories, historiesByAppId);

        log.info("[AnalyticsService] 취준 여정 조회 완료 - userId: {}, totalCount: {}", userId, totalCount);

        return JourneyResponse.builder()
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .journeyDays(journeyDays)
                .totalCount(totalCount)
                .passedCount(passedCount)
                .failedCount(failedCount)
                .inProgressCount(inProgressCount)
                .funnelStages(funnelStages)
                .monthlyTimeline(monthlyTimeline)
                .build();
    }

    private List<MonthlyTimelineResponse> buildMonthlyTimeline(
            List<Application> apps,
            List<ApplicationHistory> allHistories,
            Map<Long, List<ApplicationHistory>> historiesByAppId) {

        // [appliedCount, interviewCount, passedCount, failedCount]
        Map<YearMonth, int[]> timelineMap = new TreeMap<>();

        for (Application app : apps) {
            YearMonth ym = YearMonth.from(app.getAppliedAt());
            timelineMap.computeIfAbsent(ym, k -> new int[4])[0]++;
        }

        for (ApplicationHistory h : allHistories) {
            if (h.getScheduledAt() != null) {
                YearMonth ym = YearMonth.from(h.getScheduledAt().toLocalDate());
                timelineMap.computeIfAbsent(ym, k -> new int[4])[1]++;
            }
        }

        for (Application app : apps) {
            ApplicationResult result = app.getResult();
            if (result != ApplicationResult.PASSED && result != ApplicationResult.FAILED) {
                continue;
            }
            int idx = result == ApplicationResult.PASSED ? 2 : 3;
            historiesByAppId.getOrDefault(app.getId(), List.of()).stream()
                    .max(Comparator.comparingLong(ApplicationHistory::getId))
                    .filter(h -> h.getCompletedAt() != null)
                    .ifPresent(h -> {
                        YearMonth ym = YearMonth.from(h.getCompletedAt().toLocalDate());
                        timelineMap.computeIfAbsent(ym, k -> new int[4])[idx]++;
                    });
        }

        return timelineMap.entrySet().stream()
                .map(e -> MonthlyTimelineResponse.builder()
                        .yearMonth(e.getKey().toString())
                        .appliedCount(e.getValue()[0])
                        .interviewCount(e.getValue()[1])
                        .passedCount(e.getValue()[2])
                        .failedCount(e.getValue()[3])
                        .build())
                .toList();
    }

    private boolean isNoHistory(List<ApplicationHistory> histories) {
        return histories.isEmpty()
                || (histories.size() == 1 && histories.get(0).getStageResult() == StageResult.PENDING);
    }

    private boolean isCodeTestStage(String stage) {
        String normalized = stage.replaceAll("\\s", "");
        return CODE_TEST_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    private List<FunnelStageResponse> buildFunnel(List<ApplicationHistory> histories) {
        // [passCount, failCount, pendingCount]
        Map<String, int[]> stageCountMap = new LinkedHashMap<>();

        for (ApplicationHistory h : histories) {
            String stageName = h.getStage().trim();
            stageCountMap.computeIfAbsent(stageName, k -> new int[3]);
            int[] counts = stageCountMap.get(stageName);
            switch (h.getStageResult()) {
                case PASS -> counts[0]++;
                case FAIL -> counts[1]++;
                case PENDING -> counts[2]++;
            }
        }

        // NOTE: 결과 확정 건수(pass+fail) 기준 최저 통과율 단계를 maxDrop으로 표시
        String maxDropStageName = stageCountMap.entrySet().stream()
                .filter(e -> (e.getValue()[0] + e.getValue()[1]) > 0)
                .min(Comparator.comparingDouble(e -> (double) e.getValue()[0] / (e.getValue()[0] + e.getValue()[1])))
                .map(Map.Entry::getKey)
                .orElse(null);

        return stageCountMap.entrySet().stream()
                .map(entry -> {
                    int[] c = entry.getValue();
                    int confirmed = c[0] + c[1];
                    Double passRate = confirmed > 0
                            ? Math.round((double) c[0] / confirmed * 1000.0) / 10.0
                            : null;
                    return FunnelStageResponse.builder()
                            .stageName(entry.getKey())
                            .passCount(c[0])
                            .failCount(c[1])
                            .pendingCount(c[2])
                            .passRate(passRate)
                            .maxDrop(Objects.equals(entry.getKey(), maxDropStageName))
                            .build();
                })
                .toList();
    }

    private List<StageDurationResponse> buildStageDurations(
            Map<Long, List<ApplicationHistory>> historiesByApp) {

        // NOTE: stage명에 특수문자 포함 가능성 때문에 nested map 사용 (문자열 구분자 방식 금지)
        Map<String, Map<String, List<Long>>> durationMap = new LinkedHashMap<>();

        for (List<ApplicationHistory> histories : historiesByApp.values()) {
            for (int i = 0; i < histories.size() - 1; i++) {
                ApplicationHistory from = histories.get(i);
                ApplicationHistory to = histories.get(i + 1);

                if (from.getCompletedAt() == null) {
                    continue;
                }

                // to 기준: scheduledAt 우선, 없으면 completedAt
                LocalDateTime toReference = to.getScheduledAt() != null
                        ? to.getScheduledAt() : to.getCompletedAt();
                if (toReference == null) {
                    continue;
                }

                long days = ChronoUnit.DAYS.between(
                        from.getCompletedAt().toLocalDate(),
                        toReference.toLocalDate());

                if (days < 0 || days > 365) {
                    continue;
                }

                durationMap
                        .computeIfAbsent(from.getStage().trim(), k -> new LinkedHashMap<>())
                        .computeIfAbsent(to.getStage().trim(), k -> new ArrayList<>())
                        .add(days);
            }
        }

        return durationMap.entrySet().stream()
                .flatMap(fromEntry -> fromEntry.getValue().entrySet().stream()
                        .map(toEntry -> {
                            List<Long> days = toEntry.getValue();
                            double avg = days.stream().mapToLong(Long::longValue).average().orElse(0);
                            return StageDurationResponse.builder()
                                    .fromStage(fromEntry.getKey())
                                    .toStage(toEntry.getKey())
                                    .avgDays(Math.round(avg * 10.0) / 10.0)
                                    .sampleCount(days.size())
                                    .build();
                        }))
                .toList();
    }
}