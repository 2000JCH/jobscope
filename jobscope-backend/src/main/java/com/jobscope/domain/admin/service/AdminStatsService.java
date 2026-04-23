package com.jobscope.domain.admin.service;

import com.jobscope.domain.admin.dto.response.AdminStatsResponse;
import com.jobscope.domain.admin.dto.response.CompanyRankResponse;
import com.jobscope.domain.admin.dto.response.DailyStatResponse;
import com.jobscope.domain.alarm.service.AlarmService;
import com.jobscope.domain.application.service.ApplicationService;
import com.jobscope.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminStatsService {

    private final UserService userService;
    private final AlarmService alarmService;
    private final ApplicationService applicationService;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int ACTIVE_USER_DAYS = 30;
    private static final int DAILY_TREND_DAYS = 7;
    private static final int TOP_COMPANY_LIMIT = 10;

    /**
     * 서비스 전체 통계를 조회한다.
     */
    public AdminStatsResponse getStats() {
        LocalDateTime now = LocalDateTime.now(KST);
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1).minusNanos(1);
        LocalDateTime activeThreshold = now.minusDays(ACTIVE_USER_DAYS);
        LocalDateTime dailyTrendStart = now.minusDays(DAILY_TREND_DAYS);

        long totalUsers = userService.getTotalUserCount();
        long newUsersToday = userService.getNewUserCountSince(startOfToday);
        long activeUsers = userService.getActiveUserCountSince(activeThreshold);
        List<DailyStatResponse> dailySignups = toDailyStats(userService.getDailySignupsRaw(dailyTrendStart));

        long totalSentToday = alarmService.countTodayAlarms(startOfToday, endOfToday);
        long totalFailedToday = alarmService.countTodayAlarmFailures(startOfToday, endOfToday);

        double avgApplications = applicationService.getAvgApplicationsPerUser();
        List<CompanyRankResponse> topCompanies = toCompanyRanks(applicationService.getTopCompaniesRaw(TOP_COMPANY_LIMIT));

        log.info("[AdminStatsService] 통계 조회 완료");

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .newUsersToday(newUsersToday)
                .activeUsers(activeUsers)
                .dailyNewUsers(dailySignups)
                .totalAlarmsSentToday(totalSentToday)
                .totalAlarmsFailedToday(totalFailedToday)
                .avgApplicationsPerUser(avgApplications)
                .topCompanies(topCompanies)
                .build();
    }

    private List<DailyStatResponse> toDailyStats(List<Object[]> raw) {
        return raw.stream()
                .map(row -> DailyStatResponse.builder()
                        .date(row[0].toString())
                        .count(((Number) row[1]).longValue())
                        .build())
                .toList();
    }

    private List<CompanyRankResponse> toCompanyRanks(List<Object[]> raw) {
        return raw.stream()
                .map(row -> CompanyRankResponse.builder()
                        .companyName((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .toList();
    }
}