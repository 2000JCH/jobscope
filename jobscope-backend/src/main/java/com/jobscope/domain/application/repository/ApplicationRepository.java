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
}