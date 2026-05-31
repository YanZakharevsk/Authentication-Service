package com.innowise.auth_service.service;

import com.innowise.auth_service.jpa.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(Long userId, String token);

    RefreshToken findByRefreshToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    void revokeByUserId(Long userId);

    void deleteRefreshTokensByUserId(Long userId);
}
