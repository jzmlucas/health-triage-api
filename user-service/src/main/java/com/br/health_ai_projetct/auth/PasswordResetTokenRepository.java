package com.br.health_ai_projetct.auth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long> {
 List<PasswordResetToken> findByUserIdAndUsedFalse(Long userId);
 java.util.Optional<PasswordResetToken> findByTokenHashAndUsedFalse(String tokenHash);
}
