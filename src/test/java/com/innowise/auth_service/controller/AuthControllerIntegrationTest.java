package com.innowise.auth_service.controller;

import com.innowise.auth_service.AbstractIntegrationTest;
import com.innowise.auth_service.dto.request.LoginRequest;
import com.innowise.auth_service.dto.request.RefreshRequest;
import com.innowise.auth_service.dto.request.RegisterRequest;
import com.innowise.auth_service.dto.response.AuthResponse;
import com.innowise.auth_service.jpa.entity.Credentials;
import com.innowise.auth_service.jpa.enums.UserRole;
import com.innowise.auth_service.jpa.repository.CredentialsRepository;
import com.innowise.auth_service.jpa.repository.RefreshTokenRepository;
import com.innowise.auth_service.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CredentialsRepository credentialsRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void cleanUp() {
        refreshTokenRepository.deleteAll();
        credentialsRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration: Successful user registration returns 201")
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUserId(100L);
        request.setLogin("test_user");
        request.setPassword("strong_password");
        request.setUserRole(UserRole.USER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(100L))
                .andExpect(jsonPath("$.login").value("test_user"))
                .andExpect(jsonPath("$.userRole").value("USER"));

        assertThat(credentialsRepository.existsByLogin("test_user")).isTrue();
    }

    @Test
    @DisplayName("Integration: Error 409 when attempting to use an existing login")
    void register_Conflict_UsernameExists() throws Exception {

        Credentials existing = new Credentials(null, 101L, "busy_login", "hash", UserRole.USER);
        credentialsRepository.save(existing);

        RegisterRequest request = new RegisterRequest();
        request.setUserId(102L);
        request.setLogin("busy_login");
        request.setPassword("some_password");
        request.setUserRole(UserRole.USER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("USERNAME_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("Integration: A successful login generates tokens and saves a refresh to the database.")
    void login_Success_ShouldReturnTokens() throws Exception {
        Credentials user = new Credentials(null, 200L, "login_me", passwordEncoder.encode("correct_pass"), UserRole.USER);
        credentialsRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setLogin("login_me");
        request.setPassword("correct_pass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        assertThat(refreshTokenRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Integration: Token refresh via /refresh")
    void refresh_Success() throws Exception {
        Credentials user = new Credentials(null, 300L, "refresh_user", "hash", UserRole.USER);
        credentialsRepository.save(user);

        String rawRefreshToken = jwtTokenProvider.createRefreshToken(user.getId(), UserRole.USER);
        mockMvc.perform(post("/api/auth/login")
        );

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin("refresh_user");
        user.setPassword(passwordEncoder.encode("pass"));
        credentialsRepository.save(user);
        loginRequest.setPassword("pass");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), AuthResponse.class);

        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(authResponse.getRefreshToken());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }


    @Test
    @DisplayName("Integration: Admin can delete credentials (Honest status 204)")
    void deleteCredentials_AsAdmin_Success() throws Exception {
        Credentials victim = new Credentials(null, 500L, "victim", "hash", UserRole.USER);
        credentialsRepository.save(victim);

        String adminToken = jwtTokenProvider.createAccessToken(777L, UserRole.ADMIN);

        mockMvc.perform(delete("/api/auth/credentials/{userId}", 500L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertThat(credentialsRepository.findByUserId(500L)).isEmpty();
    }

    @Test
    @DisplayName("Integration: A regular user gets a 403 Forbidden when trying to delete")
    void deleteCredentials_AsUser_Forbidden() throws Exception {
        String userToken = jwtTokenProvider.createAccessToken(123L, UserRole.USER);

        mockMvc.perform(delete("/api/auth/credentials/{userId}", 500L)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Integration: An anonymous user without a token receives a 401 Unauthorized response")
    void deleteCredentials_AsAnonymous_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/auth/credentials/{userId}", 500L))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("Integration: DTO validation error (minimum password length)")
    void register_ValidationException_ShortPassword() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUserId(999L);
        request.setLogin("valid_login");
        request.setPassword("short");
        request.setUserRole(UserRole.USER);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message", containsString("password")));
    }
}