package com.br.health_ai_projetct.auth;

import com.br.health_ai_projetct.exception.DuplicateEmailException;
import com.br.health_ai_projetct.model.User;
import com.br.health_ai_projetct.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthService {
    private static final long RESET_TOKEN_TTL_SECONDS = 900;

    private final UserRepository users;
    private final PasswordResetTokenRepository resets;
    private final PasswordEncoder encoder;
    private final JavaMailSender mail;
    private final String secret;
    private final long ttl;
    private final String resetBaseUrl;
    private final boolean mailEnabled;

    public AuthService(
            UserRepository users,
            PasswordResetTokenRepository resets,
            PasswordEncoder encoder,
            JavaMailSender mail,
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.ttl-seconds:900}") long ttl,
            @Value("${app.password-reset-url:http://localhost:3000/reset-password}") String resetBaseUrl,
            @Value("${app.mail.enabled:false}") boolean mailEnabled
    ) {
        this.users = users;
        this.resets = resets;
        this.encoder = encoder;
        this.mail = mail;
        this.secret = secret;
        this.ttl = ttl;
        this.resetBaseUrl = resetBaseUrl;
        this.mailEnabled = mailEnabled;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validatePasswordBytes(request.password());
        String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException("Email já cadastrado");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(request.password()));
        return token(users.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        if (user.getPasswordHash() == null || !encoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }
        return token(user);
    }

    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request) {
        validatePasswordBytes(request.newPassword());
        User user = users.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        if (user.getPasswordHash() == null || !encoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }
        user.setPasswordHash(encoder.encode(request.newPassword()));
    }

    @Transactional
    public void requestReset(String email) {
        users.findByEmailIgnoreCase(email.trim()).ifPresent(user -> {
            resets.findByUserIdAndUsedFalse(user.getId()).forEach(token -> token.setUsed(true));
            byte[] bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            resets.save(new PasswordResetToken(
                    user.getId(), sha256(rawToken), Instant.now().plusSeconds(RESET_TOKEN_TTL_SECONDS)));

            if (mailEnabled) {
                try {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(user.getEmail());
                    message.setSubject("Recuperação de senha");
                    message.setText("Use este link em até 15 minutos: " + resetBaseUrl + "#token=" + rawToken);
                    mail.send(message);
                } catch (org.springframework.mail.MailException ignored) {
                    // Keep the same response for known and unknown emails.
                }
            }
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        validatePasswordBytes(request.newPassword());
        if (request.token().length() > 200) {
            throw new IllegalArgumentException("Token inválido ou expirado");
        }
        PasswordResetToken token = resets.findByTokenHashAndUsedFalse(sha256(request.token()))
                .filter(candidate -> candidate.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new IllegalArgumentException("Token inválido ou expirado"));
        User user = users.findById(token.getUserId()).orElseThrow();
        user.setPasswordHash(encoder.encode(request.newPassword()));
        token.setUsed(true);
    }

    private String sha256(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void validatePasswordBytes(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("A senha deve ter no máximo 72 bytes em UTF-8");
        }
    }

    private AuthResponse token(User user) {
        Instant now = Instant.now();
        String jwt = Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(now.plusSeconds(ttl)))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
        return new AuthResponse(jwt, "Bearer", ttl, user.getId(), user.getName(), user.getEmail());
    }
}
