package com.innowise.auth_service.dto.response;

import com.innowise.auth_service.jpa.enums.UserRole;
import lombok.Data;

@Data
public class RegisterResponse {
    private Long userId;
    private String login;
    private UserRole userRole;
}
