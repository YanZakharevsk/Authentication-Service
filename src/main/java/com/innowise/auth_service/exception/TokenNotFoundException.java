package com.innowise.auth_service.exception;

import org.springframework.http.HttpStatus;

public class TokenNotFoundException extends BaseException {
    public TokenNotFoundException(String token) {
        super("Token " + token + " not found", "NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
