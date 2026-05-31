package com.innowise.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValidateTokenRequest {
    @NotNull(message = "Access token can not be empty")
    @NotBlank(message = "Access token can not be blank")
    private String accessToken;
}
