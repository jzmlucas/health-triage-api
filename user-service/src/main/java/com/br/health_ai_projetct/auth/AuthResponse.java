package com.br.health_ai_projetct.auth;
public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, Long userId, String name, String email) {}
