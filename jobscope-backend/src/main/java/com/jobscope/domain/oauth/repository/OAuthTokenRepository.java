package com.jobscope.domain.oauth.repository;

import com.jobscope.domain.oauth.entity.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {

    Optional<OAuthToken> findByUserIdAndProvider(Long userId, String provider);

    void deleteByUserId(Long userId);
}