package com.innowise.auth_service.service;

import com.innowise.auth_service.dto.request.LoginRequest;
import com.innowise.auth_service.dto.request.RefreshRequest;
import com.innowise.auth_service.dto.request.RegisterRequest;
import com.innowise.auth_service.dto.request.ValidateTokenRequest;
import com.innowise.auth_service.dto.response.AuthResponse;
import com.innowise.auth_service.dto.response.RegisterResponse;
import com.innowise.auth_service.dto.response.TokenValidationResponse;
public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshRequest request);

    TokenValidationResponse validateToken(ValidateTokenRequest request);

    void logout(Long userId);

    Long getUserIdByLogin(String login);

    boolean deleteCredentials(Long userId);
}
