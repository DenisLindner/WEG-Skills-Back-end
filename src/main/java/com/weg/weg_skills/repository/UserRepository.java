package com.weg.weg_skills.repository;

import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByEmailIgnoreCase(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    @EntityGraph(attributePaths = "image")
    Page<User> findAllByRole(UserRole role, Pageable pageable);
}
