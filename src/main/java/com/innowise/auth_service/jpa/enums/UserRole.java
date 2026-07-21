package com.innowise.auth_service.jpa.enums;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

public enum UserRole implements GrantedAuthority {
    ADMIN, USER;

    @Override
    public @Nullable String getAuthority() {

        return name();
    }
}
