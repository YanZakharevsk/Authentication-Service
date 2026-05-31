package com.innowise.auth_service.dto.mapper;

import com.innowise.auth_service.dto.request.RegisterRequest;
import com.innowise.auth_service.dto.response.RegisterResponse;
import com.innowise.auth_service.jpa.entity.Credentials;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userRole", ignore = true)
    Credentials toCredentials(RegisterRequest request);

    RegisterResponse toRegisterResponse(Credentials credentials);
}
