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
import com.innowise.auth_service.exception.InvalidCredentialsException;
import com.innowise.auth_service.exception.InvalidJwtTokenException;
import com.innowise.auth_service.exception.UserNotFoundException;
import com.innowise.auth_service.exception.UsernameAlreadyExistsException;
import com.innowise.auth_service.exception.*;
import com.innowise.auth_service.jpa.entity.Credentials;
import com.innowise.auth_service.jpa.entity.RefreshToken;
import com.innowise.auth_service.jpa.enums.UserRole;
import com.innowise.auth_service.jpa.repository.CredentialsRepository;
import com.innowise.auth_service.jpa.repository.RefreshTokenRepository;
import com.innowise.auth_service.security.JwtTokenProvider;
import com.innowise.auth_service.service.AuthService;
import com.innowise.auth_service.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.innowise.auth_service.security.JwtTokenProvider;
import com.innowise.auth_service.service.AuthService;
import com.innowise.auth_service.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import com.innowise.auth_service.jpa.repository.RefreshTokenRepository;
import com.innowise.auth_service.security.JwtTokenProvider;
import com.innowise.auth_service.service.AuthService;
import com.innowise.auth_service.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final CredentialsRepository credentialsRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;


    public AuthServiceImpl(CredentialsRepository credentialsRepository, RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository, AuthMapper mapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.credentialsRepository = credentialsRepository;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    @Override
    public RegisterResponse register(RegisterRequest request) {
        if(!credentialsRepository.existsByLogin(request.getLogin())){

            Credentials credentials = mapper.toCredentials(request);
            credentials.setUserRole(UserRole.USER);
            credentials.setPassword(passwordEncoder.encode(request.getPassword()));

            return mapper.toRegisterResponse(credentialsRepository.save(credentials));
        }else{
            throw new UsernameAlreadyExistsException();
        }
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getLogin(),
                            request.getPassword())
            );

            Credentials credentials = credentialsRepository.findByLogin(request.getLogin())
                    .orElseThrow(() -> new CredentialsNotFoundException());

            String accessToken = jwtTokenProvider.createAccessToken(credentials.getUserId(), credentials.getUserRole());
            String refreshToken = jwtTokenProvider.createRefreshToken(credentials.getUserId(), credentials.getUserRole());

            refreshTokenService.createRefreshToken(credentials.getUserId(), refreshToken);

            AuthResponse response = new AuthResponse();
            response.setUserId(credentials.getUserId());
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);

            return response;
        }catch (AuthenticationException ex){
            throw new InvalidCredentialsException();
        }
    }

    @Override
    public AuthResponse refreshToken(RefreshRequest request) {
        RefreshToken storeRefreshToken = refreshTokenService.findByRefreshToken(request.getRefreshToken());
        refreshTokenService.verifyExpiration(storeRefreshToken);

        Credentials credentials = credentialsRepository.findByUserId(storeRefreshToken.getUserId()).orElseThrow(() -> new CredentialsNotFoundException());
        String newAccessToken = jwtTokenProvider.createAccessToken(credentials.getUserId(), credentials.getUserRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(credentials.getUserId(), credentials.getUserRole());

        refreshTokenService.revokeByUserId(credentials.getUserId());
        refreshTokenService.createRefreshToken(credentials.getUserId(), newRefreshToken);

        AuthResponse response = new AuthResponse();
        response.setUserId(credentials.getUserId());
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);

        return response;
    }

    @Override
    public TokenValidationResponse validateToken(ValidateTokenRequest request) {

        boolean valid = jwtTokenProvider.validateToken(request.getAccessToken());

        return new TokenValidationResponse(
                jwtTokenProvider.getUserIdFromToken(request.getAccessToken()),
                jwtTokenProvider.getRoleFromToken(request.getAccessToken()),
                valid);
    }

    @Transactional
    @Override
    public void logout(Long userId) {
        refreshTokenService.revokeByUserId(userId);
    }


    @Transactional
    @Override
    public boolean deleteCredentials(Long userId) {

        Credentials credentials = credentialsRepository.findByUserId(userId).orElseThrow(() -> new CredentialsNotFoundException());

        credentialsRepository.delete(credentials);
        refreshTokenService.deleteRefreshTokensByUserId(userId);

        return true;
    }

    @Override
    public Long getUserIdByLogin(String login) {
        Credentials credentials = credentialsRepository.findByLogin(login)
                .orElseThrow(() -> new CredentialsNotFoundException());
        return credentials.getUserId();
    }

}
