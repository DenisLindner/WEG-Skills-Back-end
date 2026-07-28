package com.weg.weg_skills.repository;

import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Module;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    Boolean existsByCourseAndTitleIgnoreCase(Course course, String title);
    @EntityGraph(attributePaths = "image")
    Page<Module> findAllByCourseId(Long courseId, Pageable pageable);
    List<Module> findAllByCourseId(Long courseId);
    Long countByCourseId(Long courseId);
    Module findTopByCourseOrderByPositionDesc(Course course);
}
