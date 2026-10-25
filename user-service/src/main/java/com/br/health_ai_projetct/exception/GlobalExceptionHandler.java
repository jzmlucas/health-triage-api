package com.br.health_ai_projetct.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public java.util.Map<String, String> handleInvalidRequest(IllegalArgumentException ex) {
        return java.util.Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public java.util.Map<String, String> handleBadCredentials() {
        return java.util.Map.of("error", "Credenciais inválidas");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public java.util.Map<String, String> handleAccessDenied() {
        return java.util.Map.of("error", "Acesso negado");
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFound(UserNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateEmail(DuplicateEmailException ex) {
        return ex.getMessage();
    }
}
