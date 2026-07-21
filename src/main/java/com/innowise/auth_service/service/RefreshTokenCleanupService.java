package com.innowise.auth_service.service;

/**
 * Service interface dedicated to scheduled maintenance tasks for authentication data.
 */
public interface RefreshTokenCleanupService {

    /**
     * Scans the database and permanently deletes all refresh tokens that have exceeded their expiration date.
     * This method is typically invoked by a scheduled job (e.g., via @Scheduled) to prevent database bloat.
     */
    void cleanupExpiredTokens();
}