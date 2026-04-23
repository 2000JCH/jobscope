package com.jobscope.domain.alarm.repository;

import com.jobscope.domain.alarm.entity.AlarmLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AlarmLogRepository extends JpaRepository<AlarmLog, Long> {

    Page<AlarmLog> findByUserIdOrderBySentAtDesc(Long userId, Pageable pageable);

    void deleteByIdInAndUserId(List<Long> ids, Long userId);

    long countBySentAtBetween(LocalDateTime start, LocalDateTime end);

    long countBySentAtBetweenAndIsSuccessFalse(LocalDateTime start, LocalDateTime end);
}