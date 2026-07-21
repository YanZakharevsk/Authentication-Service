package com.innowise.auth_service.service;

import com.innowise.auth_service.jpa.entity.RefreshToken;

/**
 * Service interface for managing the lifecycle of refresh tokens.
 * Provides methods for token creation, retrieval, validation, and revocation.
 */
public interface RefreshTokenService {

    /**
     * Creates and persists a new refresh token for a specific user.
     *
     * @param userId the unique identifier of the user who owns the token
     * @param token  the raw string representation of the newly generated token
     * @return the saved {@link RefreshToken} entity
     */
    RefreshToken createRefreshToken(Long userId, String token);

    /**
     * Retrieves a refresh token entity from the database using its raw string value.
     *
     * @param token the string representation of the refresh token to find
     * @return the {@link RefreshToken} entity corresponding to the given string
     * @throws RuntimeException if the token is not found in the database
     */
    RefreshToken findByRefreshToken(String token);

    /**
     * Verifies whether a given refresh token has expired.
     * If expired, the token is typically deleted from the database.
     *
     * @param token the {@link RefreshToken} entity to check for expiration
     * @return the same {@link RefreshToken} entity if it is still valid
     * @throws RuntimeException if the token has expired
     */
    RefreshToken verifyExpiration(RefreshToken token);

    /**
     * Revokes all active refresh tokens associated with a specific user.
     * This soft-deletes or marks tokens as inactive without physically removing them.
     *
     * @param userId the unique identifier of the user whose tokens should be revoked
     */
    void revokeByUserId(Long userId);

    /**
     * Permanently deletes all refresh tokens associated with a specific user from the database.
     *
     * @param userId the unique identifier of the user whose tokens should be deleted
     */
    void deleteRefreshTokensByUserId(Long userId);
}