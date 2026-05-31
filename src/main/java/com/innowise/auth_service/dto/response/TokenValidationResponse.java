package com.innowise.auth_service.dto.response;

import com.innowise.auth_service.jpa.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenValidationResponse {
    private Long userId;
    private UserRole userRole;
    private boolean valid;
}
