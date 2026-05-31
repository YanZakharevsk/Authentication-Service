package com.innowise.auth_service.service.serviceImpl;

import com.innowise.auth_service.dto.mapper.AuthMapper;
import com.innowise.auth_service.dto.request.LoginRequest;
import com.innowise.auth_service.dto.request.RefreshRequest;
import com.innowise.auth_service.dto.request.RegisterRequest;
import com.innowise.auth_service.dto.request.ValidateTokenRequest;
import com.innowise.auth_service.dto.response.AuthResponse;
import com.innowise.auth_service.dto.response.RegisterResponse;
import com.innowise.auth_service.dto.response.TokenValidationResponse;
import com.innowise.auth_service.exception.InvalidCredentialsException;
import com.innowise.auth_service.exception.InvalidJwtTokenException;
import com.innowise.auth_service.exception.UserNotFoundException;
import com.innowise.auth_service.exception.UsernameAlreadyExistsException;
import com.innowise.auth_service.jpa.entity.Credentials;
import com.innowise.auth_service.jpa.entity.RefreshToken;
import com.innowise.auth_service.jpa.enums.UserRole;
import com.innowise.auth_service.jpa.repository.CredentialsRepository;
import com.innowise.auth_service.security.JwtTokenProvider;
import com.innowise.auth_service.service.AuthService;
import com.innowise.auth_service.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialNotFoundException;

@Service
public class AuthServiceImpl implements AuthService {

    private final CredentialsRepository credentialsRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuthMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(CredentialsRepository credentialsRepository, RefreshTokenService refreshTokenService, AuthMapper mapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager) {
        this.credentialsRepository = credentialsRepository;
        this.refreshTokenService = refreshTokenService;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if(!credentialsRepository.existsByLogin(request.getLogin())){

            Credentials credentials = mapper.toCredentials(request);
            credentials.setPassword(passwordEncoder.encode(request.getPassword()));

            return mapper.toRegisterResponse(credentialsRepository.save(credentials));
        }else{
            throw new UsernameAlreadyExistsException();
        }
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        Credentials credentials = credentialsRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new CredentialsNotFoundException(request.getLogin()));

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(credentials.getLogin(), credentials.getPassword()));

            String accessToken = jwtTokenProvider.createAccessToken(credentials.getId(), credentials.getUserRole());
            String refreshToken = jwtTokenProvider.createRefreshToken(credentials.getId(), credentials.getUserRole());

            refreshTokenService.createRefreshToken(credentials.getUserId(), refreshToken);

            AuthResponse response = new AuthResponse();
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

        AuthResponse response = new AuthResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(storeRefreshToken.toString());

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

    @Override
    public void logout(String refreshToken) {

        RefreshToken token = refreshTokenService.findByRefreshToken(refreshToken);

        token.setRevoked(true);
    }

    @Override
    public boolean deleteCredentials(Long userId) {

        Credentials credentials = credentialsRepository.findByUserId(userId).orElseThrow(() -> new CredentialsNotFoundException());

        credentialsRepository.delete(credentials);
        refreshTokenService.deleteRefreshTokenByUserId(userId);

        return true;
    }

}
