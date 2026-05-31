package com.innowise.auth_service.security;

import com.innowise.auth_service.exception.InvalidJwtTokenException;
import com.innowise.auth_service.jpa.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    @Value("${security.jwt.token.access-expire-length:900000}")
    private long accessTokenValidity;

    @Value("${security.jwt.token.refresh-expire-length:86400000}")
    private long refreshTokenValidity;

    private SecretKey signingKey;

    @PostConstruct
    protected void init(){
        try{
            byte[] keyBytes = MessageDigest.getInstance("SHA-256").digest(secretKey.getBytes(StandardCharsets.UTF_8));
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        }catch (NoSuchAlgorithmException ex){
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    public String createAccessToken(Long userId, UserRole userRole){
            Date now = new Date();
            Date validity = new Date(now.getTime() + accessTokenValidity);

            return Jwts.builder()
                    .subject(String.valueOf(userId))
                    .claim("role", userRole.name())
                    .claim("type", "access")
                    .issuedAt(now)
                    .expiration(validity)
                    .signWith(signingKey)
                    .compact();
    }

    public String createRefreshToken(Long userId, UserRole userRole) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidity);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", userRole.name())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(validity)
                .signWith(signingKey)
                .compact();
    }

    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        }catch (JwtException | IllegalArgumentException ex){
            throw new InvalidJwtTokenException("Expired or invalid JWT token");
        }
    }

    public Long getUserIdFromToken(String token){
        Claims claims = getClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public UserRole getRoleFromToken(String token){
        Claims claims = getClaims(token);
        return UserRole.valueOf(claims.get("role", String.class));
    }

    private Claims getClaims(String token){
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Authentication getAuthentication(String token){
        Long userId = getUserIdFromToken(token);
        UserRole userRole = getRoleFromToken(token);
        return new UsernamePasswordAuthenticationToken(
                userId,
                "",
                List.of(new SimpleGrantedAuthority(userRole.name())));
    }

}
