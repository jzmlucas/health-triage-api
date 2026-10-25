package com.br.health_ai_projetct.auth;

import com.br.health_ai_projetct.model.User;
import com.br.health_ai_projetct.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasswordRecoveryTests {
    @Test void emailedTokenResetsPasswordOnceAndExpiresAfterUse() {
        UserRepository users = mock(UserRepository.class);
        PasswordResetTokenRepository resets = mock(PasswordResetTokenRepository.class);
        JavaMailSender mail = mock(JavaMailSender.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        AtomicReference<PasswordResetToken> savedToken = new AtomicReference<>();
        AtomicReference<SimpleMailMessage> sentMail = new AtomicReference<>();

        User user = new User();
        user.setId(42L);
        user.setName("Dora");
        user.setEmail("dora@example.test");
        user.setPasswordHash(encoder.encode("SenhaAntigaForte!2026"));
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(users.findById(42L)).thenReturn(Optional.of(user));
        when(resets.findByUserIdAndUsedFalse(42L)).thenReturn(List.of());
        doAnswer(invocation -> { savedToken.set(invocation.getArgument(0)); return invocation.getArgument(0); })
                .when(resets).save(any(PasswordResetToken.class));
        doAnswer(invocation -> { sentMail.set(invocation.getArgument(0)); return null; })
                .when(mail).send(any(SimpleMailMessage.class));

        AuthService service = new AuthService(users, resets, encoder, mail,
                "test-secret-that-is-at-least-thirty-two-bytes-long", 900,
                "https://example.test/reset-password", true);
        service.requestReset(user.getEmail());

        String text = sentMail.get().getText();
        String token = text.substring(text.indexOf("#token=") + "#token=".length());
        when(resets.findByTokenHashAndUsedFalse(anyString())).thenAnswer(invocation -> {
            PasswordResetToken candidate = savedToken.get();
            return candidate != null && candidate.getTokenHash().equals(invocation.getArgument(0)) && !candidate.isUsed()
                    ? Optional.of(candidate) : Optional.empty();
        });

        service.resetPassword(new ResetPasswordRequest(token, "SenhaNovaForte!2026"));
        assertThat(encoder.matches("SenhaNovaForte!2026", user.getPasswordHash())).isTrue();
        assertThat(savedToken.get().isUsed()).isTrue();
        assertThatThrownBy(() -> service.resetPassword(new ResetPasswordRequest(token, "TerceiraSenhaForte!2026")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Token inválido ou expirado");
    }
}
