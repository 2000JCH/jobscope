package com.jobscope.domain.alarm.repository;

import com.jobscope.domain.alarm.entity.AlarmLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmLogRepository extends JpaRepository<AlarmLog, Long> {

    Page<AlarmLog> findByUserIdOrderBySentAtDesc(Long userId, Pageable pageable);

    void deleteByIdInAndUserId(List<Long> ids, Long userId);
}