package com.weg.weg_skills.repository;

import com.weg.weg_skills.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Boolean existsByTitleIgnoreCase(String title);
    Page<Course> findAllOrderByCreatedAtDesc(Pageable pageable);
}
