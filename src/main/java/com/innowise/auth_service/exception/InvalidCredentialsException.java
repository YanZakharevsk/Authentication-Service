package com.innowise.auth_service.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException() {
        super("Invalid username/password supplied","INVALID_CREDENTIAL", HttpStatus.UNAUTHORIZED);
    }
}
