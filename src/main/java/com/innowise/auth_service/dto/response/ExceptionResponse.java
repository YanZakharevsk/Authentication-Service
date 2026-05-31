package com.innowise.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponse {
    private String errorCode;
    private String message;
    private int status;
    private Instant timestamp;
    private String path;
}
