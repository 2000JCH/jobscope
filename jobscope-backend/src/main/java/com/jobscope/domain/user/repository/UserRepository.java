package com.jobscope.domain.user.repository;

import com.jobscope.domain.user.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoId(String kakaoId);

    long countByCreatedAtAfter(LocalDateTime createdAt);

    long countByLastLoginAtAfter(LocalDateTime lastLoginAt);

    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as cnt FROM users WHERE created_at >= :since GROUP BY DATE(created_at) ORDER BY date ASC",
           nativeQuery = true)
    List<Object[]> findDailySignupsSince(@Param("since") LocalDateTime since);
}