package com.innowise.auth_service.service.serviceImpl;

import com.innowise.auth_service.jpa.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCleanupServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenCleanupServiceImpl cleanupService;

    @Test
    void cleanupExpiredTokens_ShouldCallRepositoryDelete() {
        cleanupService.cleanupExpiredTokens();

        verify(refreshTokenRepository).deleteByExpiresAtBefore(any(Instant.class));
    }
}