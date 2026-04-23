package com.jobscope.domain.application.repository;

import com.jobscope.domain.application.entity.Application;
import com.jobscope.domain.application.entity.ApplicationResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("SELECT a FROM Application a WHERE a.userId = :userId " +
           "AND (:search IS NULL OR LOWER(a.companyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Application> findByUserIdWithSearch(
            @Param("userId") Long userId,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT a FROM Application a WHERE a.userId = :userId " +
           "AND (:search IS NULL OR LOWER(a.companyName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND a.result IN :results")
    Page<Application> findByUserIdWithSearchAndResults(
            @Param("userId") Long userId,
            @Param("search") String search,
            @Param("results") List<ApplicationResult> results,
            Pageable pageable);

    long countByUserId(Long userId);

    long countByUserIdAndResult(Long userId, ApplicationResult result);

    @Query("SELECT a FROM Application a WHERE a.userId = :userId " +
           "AND a.deadlineAt IS NOT NULL " +
           "AND a.deadlineAt BETWEEN :start AND :end " +
           "ORDER BY a.deadlineAt ASC")
    List<Application> findImminentDeadlines(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Application a WHERE a.userId = :userId " +
           "AND a.deadlineAt IS NOT NULL " +
           "AND a.deadlineAt BETWEEN :start AND :end")
    List<Application> findByUserIdAndDeadlineAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // NOTE: AlarmScheduler 전용 — alarm_enabled=true이고 deadline_at이 지정 범위인 지원건 조회
    // JPQL 사용으로 @SQLRestriction("deleted_at IS NULL") 자동 적용됨
    @Query("SELECT a FROM Application a WHERE a.alarmEnabled = true " +
           "AND a.deadlineAt IS NOT NULL " +
           "AND a.deadlineAt BETWEEN :start AND :end")
    List<Application> findAlarmDeadlineTargets(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // NOTE: 회원탈퇴 전용 — 해당 유저의 모든 지원을 소프트 삭제
    @Query("SELECT a FROM Application a WHERE a.userId = :userId")
    List<Application> findAllByUserIdIgnoreDeleted(@Param("userId") Long userId);

    // NOTE: @SQLRestriction 자동 적용 — deleted_at IS NULL 필터링됨
    List<Application> findByUserId(Long userId);

    // NOTE: GET /api/alarms 알림 이력 조회용 — deleted_at IS NULL 조건 명시 (규칙 7 준수)
    // 소프트 삭제된 지원건은 반환되지 않으며, AlarmService에서 "(삭제된 지원)" fallback으로 처리됨
    @Query(value = "SELECT id, company_name FROM application WHERE id IN (:ids) AND deleted_at IS NULL",
           nativeQuery = true)
    List<Object[]> findCompanyNamesIncludingDeletedByIds(@Param("ids") List<Long> ids);
}