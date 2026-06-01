package com.innowise.auth_service.service.serviceImpl;

import com.innowise.auth_service.dto.mapper.AuthMapper;
import com.innowise.auth_service.dto.request.LoginRequest;
import com.innowise.auth_service.dto.request.RefreshRequest;
import com.innowise.auth_service.dto.request.RegisterRequest;
import com.innowise.auth_service.dto.request.ValidateTokenRequest;
import com.innowise.auth_service.dto.response.AuthResponse;
import com.innowise.auth_service.dto.response.RegisterResponse;
import com.innowise.auth_service.dto.response.TokenValidationResponse;
import com.innowise.auth_service.exception.*;
import com.innowise.auth_service.jpa.entity.Credentials;
import com.innowise.auth_service.jpa.entity.RefreshToken;
import com.innowise.auth_service.jpa.enums.UserRole;
import com.innowise.auth_service.jpa.repository.CredentialsRepository;
import com.innowise.auth_service.security.JwtTokenProvider;
import com.innowise.auth_service.service.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private CredentialsRepository credentialsRepository;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuthMapper mapper;
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Successful registration of a new user")
    void register_ShouldSaveUser_WhenLoginDoesNotExist() {
        RegisterRequest request = new RegisterRequest();
        request.setLogin("new_user");
        request.setPassword("raw_password");

        Credentials credentials = new Credentials();
        Credentials savedCredentials = new Credentials();
        RegisterResponse expectedResponse = new RegisterResponse();

        when(credentialsRepository.existsByLogin(request.getLogin())).thenReturn(false);
        when(mapper.toCredentials(request)).thenReturn(credentials);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
        when(credentialsRepository.save(credentials)).thenReturn(savedCredentials);
        when(mapper.toRegisterResponse(savedCredentials)).thenReturn(expectedResponse);

        RegisterResponse actualResponse = authService.register(request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(credentialsRepository).save(credentials);
    }

    @Test
    @DisplayName("Registration fails with an error if the login is already taken")
    void register_ShouldThrowException_WhenLoginExists() {
        RegisterRequest request = new RegisterRequest();
        request.setLogin("existing_user");

        when(credentialsRepository.existsByLogin(request.getLogin())).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> authService.register(request));
        verify(credentialsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Successful login and token generation")
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setLogin("admin");
        request.setPassword("password");

        Credentials credentials = new Credentials(1L, 10L, "admin", "encoded_password", UserRole.ADMIN);

        when(credentialsRepository.findByLogin(request.getLogin())).thenReturn(Optional.of(credentials));
        when(jwtTokenProvider.createAccessToken(1L, UserRole.ADMIN)).thenReturn("access_token");
        when(jwtTokenProvider.createRefreshToken(1L, UserRole.ADMIN)).thenReturn("refresh_token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(refreshTokenService).createRefreshToken(10L, "refresh_token");
    }

    @Test
    @DisplayName("Successful login and token generation")
    void login_ShouldThrowInvalidCredentialsException_WhenAuthenticationFails() {
        LoginRequest request = new LoginRequest();
        request.setLogin("user");
        request.setPassword("wrong_password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }


    @Test
    @DisplayName("Successful renewal of Access token with a valid Refresh token")
    void refreshToken_ShouldReturnNewAccessToken_WhenRefreshTokenIsValid() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("valid_refresh");

        RefreshToken storedToken = new RefreshToken(1L, 10L, "valid_refresh", Instant.now().plusSeconds(60), false);
        Credentials credentials = new Credentials(1L, 10L, "user", "pass", UserRole.USER);

        when(refreshTokenService.findByRefreshToken("valid_refresh")).thenReturn(storedToken);
        when(credentialsRepository.findByUserId(10L)).thenReturn(Optional.of(credentials));
        when(jwtTokenProvider.createAccessToken(10L, UserRole.USER)).thenReturn("new_access_token");

        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
        assertEquals(storedToken.toString(), response.getRefreshToken());
        verify(refreshTokenService).verifyExpiration(storedToken);
    }

    @Test
    @DisplayName("Token validation returns the correct status and claims")
    void validateToken_ShouldReturnValidationResponse() {
        ValidateTokenRequest request = new ValidateTokenRequest();
        request.setAccessToken("token");

        when(jwtTokenProvider.validateToken("token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(10L);
        when(jwtTokenProvider.getRoleFromToken("token")).thenReturn(UserRole.USER);

        TokenValidationResponse response = authService.validateToken(request);

        assertTrue(response.isValid());
        assertEquals(10L, response.getUserId());
        assertEquals(UserRole.USER, response.getUserRole());
    }

    @Test
    @DisplayName("A successful logout revokes the user's tokens.")
    void logout_ShouldRevokeTokens() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(15L);

        authService.logout(authentication);

        verify(refreshTokenService).revokeByUserId(15L);
    }

    @Test
    @DisplayName("Successful deletion of accounts by administrator")
    void deleteCredentials_ShouldDeleteData_WhenUserExists() {
        Credentials credentials = new Credentials();
        credentials.setUserId(5L);

        when(credentialsRepository.findByUserId(5L)).thenReturn(Optional.of(credentials));

        boolean result = authService.deleteCredentials(5L);

        assertTrue(result);
        verify(credentialsRepository).delete(credentials);
        verify(refreshTokenService).deleteRefreshTokensByUserId(5L);
    }
}