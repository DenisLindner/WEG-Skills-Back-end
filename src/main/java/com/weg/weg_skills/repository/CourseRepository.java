package com.weg.weg_skills.repository;

import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.projection.CourseWithRatingProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Boolean existsByTitleIgnoreCase(String title);
    @Query("""
            SELECT c.id AS id, c.title AS title, c.description AS description, COALESCE(AVG(r.rate), 0.0) AS rating, i AS image
            FROM Course c
            LEFT JOIN c.enrollments e
            LEFT JOIN c.reviews r
            LEFT JOIN c.image i
            GROUP BY
                    c.id,
                    c.title,
                    c.description,
                    i
            ORDER BY COUNT(DISTINCT e.id) DESC
        """)
    List<CourseWithRatingProjection> findMostEnrollmentsCourses(Pageable pageable);
    Page<Course> findAllByTitleContainingIgnoreCase(String title, Pageable pageable);
}
