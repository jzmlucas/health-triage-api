package com.br.health_ai_projetct.repository;

import com.br.health_ai_projetct.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}
