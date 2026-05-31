package com.innowise.auth_service.dto.response;

import lombok.Data;

@Data
public class AuthResponse {
    private Long userId;
    private String accessToken;
    private String refreshToken;
}
