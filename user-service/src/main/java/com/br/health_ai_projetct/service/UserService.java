package com.br.health_ai_projetct.service;

import com.br.health_ai_projetct.dto.UserRequestDTO;
import com.br.health_ai_projetct.dto.UserResponseDTO;
import com.br.health_ai_projetct.exception.DuplicateEmailException;
import com.br.health_ai_projetct.exception.UserNotFoundException;
import com.br.health_ai_projetct.model.User;
import com.br.health_ai_projetct.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<UserResponseDTO> getUsers() {
        return repository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public UserResponseDTO createUser(UserRequestDTO dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("Email ja cadastrado");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        return toResponseDTO(repository.save(user));
    }

    public UserResponseDTO getUserById(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario nao encontrado"));

        return toResponseDTO(user);
    }

    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario nao encontrado"));

        if (!user.getEmail().equals(dto.getEmail()) && repository.existsByEmail(dto.getEmail())) {
            throw new DuplicateEmailException("Email ja cadastrado");
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        return toResponseDTO(repository.save(user));
    }

    public void deleteUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario nao encontrado"));

        repository.delete(user);
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail());
    }
}
