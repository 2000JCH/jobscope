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
}