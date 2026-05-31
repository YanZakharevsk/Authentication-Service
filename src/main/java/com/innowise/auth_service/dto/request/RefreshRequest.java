package com.innowise.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefreshRequest {
    @NotNull(message = "Refresh token can not be empty")
    @NotBlank(message = "Refresh token can not be blank")
    private String refreshToken;
}
