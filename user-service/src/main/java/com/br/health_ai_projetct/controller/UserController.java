package com.br.health_ai_projetct.controller;

import com.br.health_ai_projetct.dto.UserRequestDTO;
import com.br.health_ai_projetct.dto.UserResponseDTO;
import com.br.health_ai_projetct.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable Long id, Authentication authentication) {
        requireOwner(id, authentication);
        return service.getUserById(id);
    }

    @PutMapping("/{id}")
    public UserResponseDTO update(
            @PathVariable Long id,
            @RequestBody @Valid UserRequestDTO dto,
            Authentication authentication
    ) {
        requireOwner(id, authentication);
        return service.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        requireOwner(id, authentication);
        service.deleteUser(id);
    }

    private void requireOwner(Long id, Authentication authentication) {
        if (!id.toString().equals(authentication.getName())) throw new AccessDeniedException("Acesso negado");
    }
}
