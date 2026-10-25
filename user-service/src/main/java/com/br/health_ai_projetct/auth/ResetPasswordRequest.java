package com.br.health_ai_projetct.auth;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record ResetPasswordRequest(@NotBlank String token, @NotBlank @Size(min=12,max=128) String newPassword) {}
