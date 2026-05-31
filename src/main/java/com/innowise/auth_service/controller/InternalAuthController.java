package com.innowise.auth_service.controller;

import com.innowise.auth_service.dto.request.RegisterRequest;
import com.innowise.auth_service.dto.request.ValidateTokenRequest;
import com.innowise.auth_service.dto.response.RegisterResponse;
import com.innowise.auth_service.dto.response.TokenValidationResponse;
import com.innowise.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/auth")
public class InternalAuthController {

    private final AuthService authService;

    public InternalAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/credentials")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(@Valid @RequestBody ValidateTokenRequest request){

        return ResponseEntity.status(HttpStatus.OK)
                .body(authService.validateToken(request));
    }

    @DeleteMapping("/credentials/{userId}")
    public ResponseEntity<Void> deleteCredentials(@PathVariable @Valid Long userId){
        authService.deleteCredentials(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/credentials")
    public ResponseEntity<Long> getUserIdByLogin(@RequestParam String login){
        return ResponseEntity.status(HttpStatus.OK).body(authService.getUserIdByLogin(login));
    }

}
