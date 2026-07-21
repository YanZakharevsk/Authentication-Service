package com.innowise.auth_service.service;

import com.innowise.auth_service.dto.request.LoginRequest;
import com.innowise.auth_service.dto.request.RefreshRequest;
import com.innowise.auth_service.dto.request.RegisterRequest;
import com.innowise.auth_service.dto.request.ValidateTokenRequest;
import com.innowise.auth_service.dto.response.AuthResponse;
import com.innowise.auth_service.dto.response.RegisterResponse;
import com.innowise.auth_service.dto.response.TokenValidationResponse;

/**
 * Service interface for handling core authentication and authorization workflows.
 * Provides methods for user registration, logging in, token management, and session termination.
 */
public interface AuthService {

    /**
     * Registers a new user's credentials in the authentication system.
     *
     * @param request the data transfer object containing the user's registration details (e.g., login, password)
     * @return a {@link RegisterResponse} indicating the result of the registration process
     * @throws RuntimeException if the user or login already exists, or validation fails
     */
    RegisterResponse register(RegisterRequest request);

    /**
     * Authenticates a user based on their login credentials and generates access and refresh tokens.
     *
     * @param request the data transfer object containing the user's login and password
     * @return an {@link AuthResponse} containing the generated JWT access and refresh tokens
     * @throws RuntimeException if authentication fails due to incorrect credentials
     */
    AuthResponse login(LoginRequest request);

    /**
     * Generates a new set of access and refresh tokens using a valid refresh token.
     *
     * @param request the data transfer object containing the active refresh token
     * @return an {@link AuthResponse} containing the newly generated tokens
     * @throws RuntimeException if the provided refresh token is expired, invalid, or not found
     */
    AuthResponse refreshToken(RefreshRequest request);

    /**
     * Validates an access token to ensure it is correctly signed, not expired, and belongs to an active session.
     *
     * @param request the data transfer object containing the token to validate
     * @return a {@link TokenValidationResponse} detailing whether the token is valid and its associated claims
     */
    TokenValidationResponse validateToken(ValidateTokenRequest request);

    /**
     * Logs out a user by invalidating or deleting their active refresh tokens.
     *
     * @param userId the unique identifier of the user logging out
     */
    void logout(Long userId);

    /**
     * Retrieves the internal user ID associated with a specific login string.
     *
     * @param login the login (username or email) of the user
     * @return the unique {@link Long} identifier of the user
     * @throws RuntimeException if no credentials correspond to the given login
     */
    Long getUserIdByLogin(String login);

    /**
     * Permanently deletes the authentication credentials associated with a user ID.
     * Usually called when an account is deleted from the system.
     *
     * @param userId the unique identifier of the user whose credentials should be removed
     * @return {@code true} if credentials were successfully deleted, {@code false} otherwise
     */
    boolean deleteCredentials(Long userId);
}