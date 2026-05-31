package com.innowise.auth_service.service;

public interface RefreshTokenCleanupService {
    void cleanupExpiredTokens();
}
