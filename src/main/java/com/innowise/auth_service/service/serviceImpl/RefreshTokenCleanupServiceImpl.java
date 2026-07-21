package com.innowise.auth_service.service.serviceImpl;

import com.innowise.auth_service.jpa.repository.RefreshTokenRepository;
import com.innowise.auth_service.service.RefreshTokenCleanupService;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RefreshTokenCleanupServiceImpl implements RefreshTokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    @Scheduled(cron = "0 */10 * * * *")
    @Override
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
