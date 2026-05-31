package com.innowise.auth_service.exception;

import org.springframework.http.HttpStatus;

public class UsernameAlreadyExistsException extends BaseException {
    public UsernameAlreadyExistsException() {
        super("Username is already in use", "USERNAME_ALREADY_EXISTS", HttpStatus.CONFLICT);
    }
}
