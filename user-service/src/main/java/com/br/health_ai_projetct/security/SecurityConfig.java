package com.br.health_ai_projetct.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import java.util.List;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
public class SecurityConfig {
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthFilter jwtFilter)throws Exception{
  http.csrf(AbstractHttpConfigurer::disable).cors(Customizer.withDefaults()).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).headers(h->h.frameOptions(f->f.deny()))
   .exceptionHandling(e->e.authenticationEntryPoint((request,response,exception)->response.sendError(401,"Unauthorized")).accessDeniedHandler((request,response,exception)->response.sendError(403,"Forbidden")))
   .authorizeHttpRequests(a->a.requestMatchers("/actuator/health","/auth/register","/auth/login","/auth/forgot-password","/auth/reset-password").permitAll().anyRequest().authenticated())
   .addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class);return http.build();
 }
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
 @Bean UserDetailsService noLocalPasswordAuthentication(){return username->{throw new UsernameNotFoundException("Use autenticação JWT");};}
    @Bean CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins:http://localhost:3000}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(origin -> !origin.isEmpty()).toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(List.of("X-RateLimit-Limit", "X-RateLimit-Remaining", "Retry-After"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}


