package com.br.health_ai_projetct.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final SecretKey key;
    public JwtAuthFilter(@Value("${security.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(auth.substring(7)).getPayload();
                String userId = claims.getSubject();
                var authentication = new UsernamePasswordAuthenticationToken(userId, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (RuntimeException ignored) { SecurityContextHolder.clearContext(); }
        }
        chain.doFilter(request, response);
    }
}
