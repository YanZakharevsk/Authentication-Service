package com.innowise.auth_service.exception;

import org.springframework.http.HttpStatus;

public class RefreshTokenExpiredOrRevokedException extends BaseException {
    public RefreshTokenExpiredOrRevokedException(String message) {
        super(message, "TOKEN_EXPIRED_OR_REVOKED", HttpStatus.UNAUTHORIZED);
    }
}
