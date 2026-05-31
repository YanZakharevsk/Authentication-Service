package com.innowise.auth_service.service.serviceImpl;

import com.innowise.auth_service.exception.RefreshTokenExpiredOrRevokedException;
import com.innowise.auth_service.exception.TokenNotFoundException;
import com.innowise.auth_service.jpa.entity.RefreshToken;
import com.innowise.auth_service.jpa.repository.RefreshTokenRepository;
import com.innowise.auth_service.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.token.refresh-expire-length}")
    private long refreshTokenValidity;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    @Override
    public RefreshToken createRefreshToken(Long userId, String token) {
        Instant expirationAt = Instant.now().plusMillis(refreshTokenValidity);
        RefreshToken refreshToken = new RefreshToken(userId,token,expirationAt);
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken findByRefreshToken(String token) {
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByToken(token);
        RefreshToken refreshToken;
        if(!optionalRefreshToken.isEmpty()){
            refreshToken = optionalRefreshToken.get();
        }else{
           throw new TokenNotFoundException(token);
        }
        return refreshToken;
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getRevoked()) {
            throw new RefreshTokenExpiredOrRevokedException("Refresh token is revoked");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new RefreshTokenExpiredOrRevokedException("Refresh token is expired");
        }

        return token;
    }

    @Transactional
    @Override
    public void revokeByUserId(Long userId) {
        refreshTokenRepository.findAll()
                .stream()
                .filter(t -> t.getUserId().equals(userId))
                .forEach(t -> {
                    t.setRevoked(true);
                    refreshTokenRepository.save(t);
                });
    }

    @Transactional
    @Override
    public void deleteRefreshTokensByUserId(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }
}
