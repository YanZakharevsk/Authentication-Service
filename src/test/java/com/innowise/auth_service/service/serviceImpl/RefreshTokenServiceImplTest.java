package com.innowise.auth_service.service.serviceImpl;

import com.innowise.auth_service.exception.RefreshTokenExpiredOrRevokedException;
import com.innowise.auth_service.exception.TokenNotFoundException;
import com.innowise.auth_service.jpa.entity.RefreshToken;
import com.innowise.auth_service.jpa.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenValidity", 86400000L);
    }

    @Test
    @DisplayName("Refresh token creation correctly calculates time and saves it")
    void createRefreshToken_ShouldSaveTokenWithCalculatedExpiry() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken created = refreshTokenService.createRefreshToken(1L, "raw_token_string");

        assertNotNull(created);
        assertEquals(1L, created.getUserId());
        assertEquals("raw_token_string", created.getToken());
        assertFalse(created.getRevoked());
        assertTrue(created.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    @DisplayName("Token lookup throws an exception if the token is not found")
    void findByRefreshToken_ShouldThrowException_WhenNotFound() {
        when(refreshTokenRepository.findByToken("absent_token")).thenReturn(Optional.empty());

        assertThrows(TokenNotFoundException.class, () -> refreshTokenService.findByRefreshToken("absent_token"));
    }

    @Test
    @DisplayName("Lifetime check: fails if token is revoked")
    void verifyExpiration_ShouldThrowException_WhenTokenIsRevoked() {
        RefreshToken token = new RefreshToken(1L, 1L, "token", Instant.now().plusSeconds(60), true);

        assertThrows(RefreshTokenExpiredOrRevokedException.class, () -> refreshTokenService.verifyExpiration(token));
    }

    @Test
    @DisplayName("Lifetime check: crashes if lifetime expires")
    void verifyExpiration_ShouldThrowException_WhenTokenIsExpired() {
        RefreshToken token = new RefreshToken(1L, 1L, "token", Instant.now().minusSeconds(10), false);

        assertThrows(RefreshTokenExpiredOrRevokedException.class, () -> refreshTokenService.verifyExpiration(token));
    }

    @Test
    @DisplayName("Lifetime check: returns the token successfully if it is valid")
    void verifyExpiration_ShouldReturnToken_WhenItIsValid() {
        RefreshToken token = new RefreshToken(1L, 1L, "token", Instant.now().plusSeconds(100), false);

        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertEquals(token, result);
    }
}