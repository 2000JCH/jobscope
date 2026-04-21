package com.jobscope.domain.application.repository;

import com.jobscope.domain.application.entity.ApplicationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApplicationHistoryRepository extends JpaRepository<ApplicationHistory, Long> {

    List<ApplicationHistory> findByApplicationIdOrderByIdAsc(Long applicationId);

    Optional<ApplicationHistory> findTopByApplicationIdOrderByIdDesc(Long applicationId);

    // NOTE: id 최댓값 기준으로 각 applicationId별 최신 히스토리 1건씩 조회 (N+1 방지)
    @Query("SELECT h FROM ApplicationHistory h WHERE h.id IN (" +
           "SELECT MAX(h2.id) FROM ApplicationHistory h2 " +
           "WHERE h2.application.id IN :applicationIds " +
           "GROUP BY h2.application.id)")
    List<ApplicationHistory> findLatestByApplicationIds(@Param("applicationIds") List<Long> applicationIds);

    // NOTE: application join 시 @SQLRestriction("deleted_at IS NULL") 자동 적용됨 (JPQL)
    @Query("SELECT h FROM ApplicationHistory h WHERE h.application.userId = :userId " +
           "AND h.scheduledAt BETWEEN :start AND :end")
    List<ApplicationHistory> findByUserIdAndScheduledAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // NOTE: AlarmScheduler 전용 — alarm_enabled=true이고 scheduled_at이 지정 범위인 히스토리 조회
    // JOIN FETCH로 N+1 방지, @SQLRestriction으로 삭제된 지원건 자동 제외
    @Query("SELECT h FROM ApplicationHistory h JOIN FETCH h.application a " +
           "WHERE a.alarmEnabled = true " +
           "AND h.scheduledAt IS NOT NULL " +
           "AND h.scheduledAt BETWEEN :start AND :end")
    List<ApplicationHistory> findScheduledAlarmTargets(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // NOTE: GET /api/alarms 알림 이력 조회용 — historyId 배치 조회로 stage 이름 가져오기
    List<ApplicationHistory> findByIdIn(List<Long> ids);

    // NOTE: Analytics 전용 — JOIN FETCH로 N+1 방지, @SQLRestriction으로 삭제된 지원건 자동 제외
    @Query("SELECT h FROM ApplicationHistory h JOIN FETCH h.application a WHERE a.id IN :appIds")
    List<ApplicationHistory> findByApplicationIdIn(@Param("appIds") List<Long> appIds);

    // NOTE: 대시보드 넛지 전용 — 히스토리 미입력(서류/PENDING 1건만 있는 IN_PROGRESS) 지원건 수
    // Native Query 사용으로 deleted_at IS NULL 조건 직접 명시 (규칙 7 준수)
    @Query(value =
            "SELECT COUNT(*) FROM (" +
            "  SELECT h.application_id" +
            "  FROM application_history h" +
            "  INNER JOIN application a ON h.application_id = a.id" +
            "  WHERE a.user_id = :userId" +
            "  AND a.deleted_at IS NULL" +
            "  AND a.result = 'IN_PROGRESS'" +
            "  GROUP BY h.application_id" +
            "  HAVING COUNT(*) = 1" +
            "  AND SUM(CASE WHEN h.stage_result != 'PENDING' THEN 1 ELSE 0 END) = 0" +
            ") AS t",
            nativeQuery = true)
    long countUnstatedInProgressByUserId(@Param("userId") Long userId);
}