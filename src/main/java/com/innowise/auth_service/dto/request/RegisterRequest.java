package com.innowise.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {


    @NotNull(message = "User id can not be null")
    private Long userId;

    @NotNull(message = "Username can not be null")
    @NotBlank(message = "Username can not be blank")
    @Size(min = 3, max = 255, message = "Minimum username length: 3 characters")
    private String login;

    @NotNull(message = "Password can not be null")
    @NotBlank(message = "Password can not be blank")
    @Size(min = 8, message = "Minimum password length: 8 characters")
    private String password;

}
