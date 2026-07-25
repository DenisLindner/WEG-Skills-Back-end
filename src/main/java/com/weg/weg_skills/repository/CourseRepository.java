package com.weg.weg_skills.repository;

import com.weg.weg_skills.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Boolean existsByTitleIgnoreCase(String title);
    @Query("""
            SELECT c
            FROM Course c
            LEFT JOIN Enrollment e
            GROUP BY c
            ORDER BY COUNT(e) DESC
        """)
    List<Course> findMostEnrollmentsCourses(Pageable pageable);
    Page<Course> findAllByTitleIgnoreCaseOrderByCreatedAtDesc(String title, Pageable pageable);
}
