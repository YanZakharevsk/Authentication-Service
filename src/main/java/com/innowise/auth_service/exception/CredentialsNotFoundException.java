package com.innowise.auth_service.exception;

import org.springframework.http.HttpStatus;

public class CredentialsNotFoundException extends BaseException{
    public CredentialsNotFoundException() {
        super("Credentials not found", "NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
