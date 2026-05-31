package com.innowise.auth_service.exception;

import com.innowise.auth_service.dto.response.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex, HttpServletRequest httpServletRequest){
        return createExceptionResponse(ex, httpServletRequest);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidCredentialsException(InvalidCredentialsException ex, HttpServletRequest request){
        return createExceptionResponse(ex, request);
    }

    @ExceptionHandler(InvalidJwtTokenException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidJwtTokenException(InvalidJwtTokenException ex, HttpServletRequest request){
        return createExceptionResponse(ex, request);
    }

    @ExceptionHandler(CredentialsNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleCredentialsNotFoundException(CredentialsNotFoundException ex, HttpServletRequest request){
        return createExceptionResponse(ex, request);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleTokenNotFound(TokenNotFoundException ex, HttpServletRequest request){
        return createExceptionResponse(ex, request);
    }

    @ExceptionHandler(RefreshTokenExpiredOrRevokedException.class)
    public ResponseEntity<ExceptionResponse> handleRefreshTokenExpiredOrRevokedException(RefreshTokenExpiredOrRevokedException ex, HttpServletRequest request){
        return createExceptionResponse(ex, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponse> handleAuthenticationException(HttpServletRequest httpServletRequest){
        ExceptionResponse response = new ExceptionResponse(
                "AUTHENTICATION ERROR",
                "Authentication required",
                HttpStatus.UNAUTHORIZED.value(),
                Instant.now(),
                httpServletRequest.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedException(HttpServletRequest httpServletRequest){
        ExceptionResponse response = new ExceptionResponse(
                "AUTHORIZATION ERROR",
                "Access Denied",
                HttpStatus.FORBIDDEN.value(),
                Instant.now(),
                httpServletRequest.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest httpServletRequest){

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        ExceptionResponse response = new ExceptionResponse();
        response.setErrorCode("VALIDATION_ERROR");
        response.setMessage(message);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setTimestamp(Instant.now());
        response.setPath(httpServletRequest.getRequestURI());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }



    private ResponseEntity<ExceptionResponse> createExceptionResponse(BaseException ex, HttpServletRequest httpServletRequest) {
        ExceptionResponse response = new ExceptionResponse();
        response.setErrorCode(ex.getErrorCode());
        response.setMessage(ex.getMessage());
        response.setStatus(ex.getStatus().value());
        response.setTimestamp(Instant.now());
        response.setPath(httpServletRequest.getRequestURI());
        return new ResponseEntity<>(response, ex.getStatus());
    }

}
