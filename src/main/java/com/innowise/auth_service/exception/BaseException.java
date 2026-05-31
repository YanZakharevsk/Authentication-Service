package com.innowise.auth_service.exception;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor(force = true)
@Data
public class BaseException extends RuntimeException{
    private final String errorCode;
    private final HttpStatus status;

    public BaseException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
