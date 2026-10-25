package com.br.health_ai_projetct.auth;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/auth")
public class AuthController {
 private final AuthService service; public AuthController(AuthService service){this.service=service;}
 @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) public AuthResponse register(@RequestBody @Valid RegisterRequest req){return service.register(req);}
 @PostMapping("/login") public AuthResponse login(@RequestBody @Valid LoginRequest req){return service.login(req);}
 @PostMapping("/change-password") @ResponseStatus(HttpStatus.NO_CONTENT) public void change(@RequestBody @Valid ChangePasswordRequest req,Authentication auth){service.changePassword(Long.valueOf(auth.getName()),req);}
 @PostMapping("/forgot-password") public java.util.Map<String,String> forgot(@RequestBody @Valid ForgotPasswordRequest req){service.requestReset(req.email());return java.util.Map.of("message","Se o e-mail estiver cadastrado, você receberá as instruções de recuperação.");}
 @PostMapping("/reset-password") @ResponseStatus(HttpStatus.NO_CONTENT) public void reset(@RequestBody @Valid ResetPasswordRequest req){service.resetPassword(req);}
}
