package com.br.health_ai_projetct.auth;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="password_reset_tokens", indexes=@Index(name="idx_reset_user",columnList="user_id"))
public class PasswordResetToken {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false) private Long userId;
 @Column(nullable=false,length=64,unique=true) private String tokenHash;
 @Column(nullable=false) private Instant expiresAt;
 @Column(nullable=false) private boolean used=false;
 protected PasswordResetToken() {}
 public PasswordResetToken(Long userId,String tokenHash,Instant expiresAt){this.userId=userId;this.tokenHash=tokenHash;this.expiresAt=expiresAt;}
 public Long getId(){return id;} public Long getUserId(){return userId;} public String getTokenHash(){return tokenHash;} public Instant getExpiresAt(){return expiresAt;} public boolean isUsed(){return used;} public void setUsed(boolean used){this.used=used;}
}
