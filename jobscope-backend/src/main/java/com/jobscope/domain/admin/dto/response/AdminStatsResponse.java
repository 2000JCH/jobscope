package com.jobscope.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminStatsResponse {

    private long totalUsers;
    private long newUsersToday;
    private long activeUsers;
    private List<DailyStatResponse> dailyNewUsers;

    private long totalAlarmsSentToday;
    private long totalAlarmsFailedToday;

    private double avgApplicationsPerUser;
    private List<CompanyRankResponse> topCompanies;
}