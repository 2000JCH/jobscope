package com.jobscope.domain.notice.repository;

import com.jobscope.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Optional<Notice> findFirstByActiveTrueOrderByCreatedAtDesc();
}