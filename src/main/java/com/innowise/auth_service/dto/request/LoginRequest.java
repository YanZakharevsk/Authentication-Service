package com.innowise.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotNull(message = "Username can not be null")
    @NotBlank(message = "Username can not be blank")
    private String login;

    @NotNull(message = "Password can not be null")
    @NotBlank(message = "Password can not be blank")
    private String password;
}
