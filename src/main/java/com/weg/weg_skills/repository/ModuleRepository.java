package com.weg.weg_skills.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    Boolean existsByTitleIgnoreCase(String title);
}
