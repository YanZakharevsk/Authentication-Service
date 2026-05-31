package com.innowise.auth_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    @Value("${internal.api-key}")
    private String expectedApiKey;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String actualApiKey = request.getHeader("X-Internal-Key");

        if (actualApiKey == null || !actualApiKey.equals(expectedApiKey)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Direct access is forbidden !");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
