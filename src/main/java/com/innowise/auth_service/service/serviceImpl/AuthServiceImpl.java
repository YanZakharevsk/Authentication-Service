package com.innowise.auth_service.service.serviceImpl;

import com.innowise.auth_service.dto.mapper.AuthMapper;
import com.innowise.auth_service.dto.request.LoginRequest;
import com.innowise.auth_service.dto.request.RefreshRequest;
import com.innowise.auth_service.dto.request.RegisterRequest;
import com.innowise.auth_service.dto.request.ValidateTokenRequest;
import com.innowise.auth_service.dto.response.AuthResponse;
import com.innowise.auth_service.dto.response.RegisterResponse;
import com.innowise.auth_service.dto.response.TokenValidationResponse;
<<<<<<< HEAD
import com.innowise.auth_service.exception.*;
=======
import com.innowise.auth_service.exception.InvalidCredentialsException;
import com.innowise.auth_service.exception.InvalidJwtTokenException;
import com.innowise.auth_service.exception.UserNotFoundException;
import com.innowise.auth_service.exception.UsernameAlreadyExistsException;
>>>>>>> a4ac0c0 (Auth service has been written for proccessing the main business logic for work with credentials)
import com.innowise.auth_service.jpa.entity.Credentials;
import com.innowise.auth_service.jpa.entity.RefreshToken;
import com.innowise.auth_service.jpa.enums.UserRole;
import com.innowise.auth_service.jpa.repository.CredentialsRepository;
<<<<<<< HEAD
import com.innowise.auth_service.jpa.repository.RefreshTokenRepository;
import com.innowise.auth_service.security.JwtTokenProvider;
import com.innowise.auth_service.service.AuthService;
import com.innowise.auth_service.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
=======
import com.innowise.auth_service.security.JwtTokenProvider;
import com.innowise.auth_service.service.AuthService;
import com.innowise.auth_service.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
>>>>>>> a4ac0c0 (Auth service has been written for proccessing the main business logic for work with credentials)
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

<<<<<<< HEAD

@Service
@Slf4j
=======
import javax.security.auth.login.CredentialNotFoundException;

@Service
>>>>>>> a4ac0c0 (Auth service has been written for proccessing the main business logic for work with credentials)
public class AuthServiceImpl implements AuthService {

    private final CredentialsRepository credentialsRepository;
    private final RefreshTokenService refreshTokenService;
<<<<<<< HEAD
    private final RefreshTokenRepository refreshTokenRepository;
=======
>>>>>>> a4ac0c0 (Auth service has been written for proccessing the main business logic for work with credentials)
    private final AuthMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

<<<<<<< HEAD
    public AuthServiceImpl(CredentialsRepository credentialsRepository, RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository, AuthMapper mapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.credentialsRepository = credentialsRepository;
        this.refreshTokenService = refreshTokenService;
        this.refreshTokenRepository = refreshTokenRepository;
=======
    public AuthServiceImpl(CredentialsRepository credentialsRepository, RefreshTokenService refreshTokenService, AuthMapper mapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.credentialsRepository = credentialsRepository;
        this.refreshTokenService = refreshTokenService;
>>>>>>> a4ac0c0 (Auth service has been written for proccessing the main business logic for work with credentials)
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

<<<<<<< HEAD
    @Transactional
=======
>>>>>>> a4ac0c0 (Auth service has been written for proccessing the main business logic for work with credentials)
    @Override
    public RegisterResponse register(RegisterRequest request) {
        if(!credentialsRepository.existsByLogin(request.getLogin())){

            Credentials credentials = mapper.toCredentials(request);
<<<<<<< HEAD
            credentials.setUserRole(UserRole.USER);
=======
>>>>>>> a4ac0c0 (Auth service has been written for proccessing the main business logic for work with credentials)
            credentials.setPassword(passwordEncoder.encode(request.getPassword()));

            return mapper.toRegisterResponse(credentialsRepository.save(credentials));
        }else{
            throw new UsernameAlreadyExistsException();
        }
    }

    @Override
    public AuthResponse login(LoginRequest request) {

<<<<<<< HEAD
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
=======
        Credentials credentials = credentialsRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new CredentialsNotFoundException(request.getLogin()));

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(credentials.getLogin(), credentials.getPassword()));

            String accessToken = jwtTokenProvider.createAccessToken(credentials.getId(), credentials.getUserRole());
            String refreshToken = jwtTokenProvider.createRefreshToken(credentials.getId(), credentials.getUserRole());
>>>>>>> a4ac0c0 (Auth service has been written for proccessing the main business logic for work with credentials)

            refreshTokenService.createRefreshToken(credentials.getUserId(), refreshToken);

            AuthResponse response = new AuthResponse();
<<<<<<< HEAD
            response.setUserId(credentials.getUserId());
=======
>>>>>>> a4ac0c0 (Auth service has been written for proccessing the main business logic for work with credentials)
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);

            return response;
        }catch (AuthenticationException ex){
            throw new InvalidCredentialsException();
        }
    }

    @Override
    public AuthResponse refreshToken(RefreshRequest request) {
<<<<<<< HEAD
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
=======

        RefreshToken storeRefreshToken = refreshTokenService.findByRefreshToken(request.getRefreshToken());

        refreshTokenService.verifyExpiration(storeRefreshToken);

        Credentials credentials = credentialsRepository.findByUserId(storeRefreshToken.getUserId()).orElseThrow(() -> new CredentialsNotFoundException());

        String newAccessToken = jwtTokenProvider.createAccessToken(credentials.getUserId(), credentials.getUserRole());

        AuthResponse response = new AuthResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(storeRefreshToken.toString());
>>>>>>> a4ac0c0 (Auth service has been written for proccessing the main business logic for work with credentials)

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

<<<<<<< HEAD
    @Transactional
    @Override
    public void logout(Long userId) {
        refreshTokenService.revokeByUserId(userId);
    }

    @Transactional
=======
    @Override
    public void logout(String refreshToken) {

        RefreshToken token = refreshTokenService.findByRefreshToken(refreshToken);

        token.setRevoked(true);
    }

>>>>>>> a4ac0c0 (Auth service has been written for proccessing the main business logic for work with credentials)
    @Override
    public boolean deleteCredentials(Long userId) {

        Credentials credentials = credentialsRepository.findByUserId(userId).orElseThrow(() -> new CredentialsNotFoundException());

        credentialsRepository.delete(credentials);
<<<<<<< HEAD
        refreshTokenService.deleteRefreshTokensByUserId(userId);
=======
        refreshTokenService.deleteRefreshTokenByUserId(userId);
>>>>>>> a4ac0c0 (Auth service has been written for proccessing the main business logic for work with credentials)

        return true;
    }

<<<<<<< HEAD
    @Override
    public Long getUserIdByLogin(String login) {
        Credentials credentials = credentialsRepository.findByLogin(login)
                .orElseThrow(() -> new CredentialsNotFoundException());
        return credentials.getUserId();
    }

=======
>>>>>>> a4ac0c0 (Auth service has been written for proccessing the main business logic for work with credentials)
}
