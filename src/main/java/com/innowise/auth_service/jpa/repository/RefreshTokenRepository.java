package com.innowise.auth_service.jpa.repository;

import com.innowise.auth_service.jpa.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserId(Long userId);

    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);

    void deleteByExpiresAtBefore(Instant expiresAtBefore);

    boolean existsByToken(String token);

    void deleteAllByUserId(Long userId);
    boolean deleteByExpiresAtBefore(Instant expiresAtBefore);
}
