package com.weg.weg_skills.repository;

import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Module;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    Boolean existsByCourseAndTitleIgnoreCase(Course course, String title);
    Page<Module> findAllByCourseIdOrderByCreatedAtDesc(Long courseId, Pageable pageable);
}
