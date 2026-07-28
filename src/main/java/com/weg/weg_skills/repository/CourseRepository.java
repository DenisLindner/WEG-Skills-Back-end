package com.weg.weg_skills.repository;

import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.projection.CourseWithRatingProjection;
import com.weg.weg_skills.projection.ProgressProjection;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Boolean existsByTitleIgnoreCase(String title);

    @Override
    @EntityGraph(attributePaths = "image")
    @NonNull
    Page<Course> findAll(@NonNull Pageable pageable);

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
    @EntityGraph(attributePaths = "image")
    Page<Course> findAllByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("""
            SELECT
                c.id AS courseId,
                COUNT(DISTINCT p.id) AS completedLessons,
                COUNT(DISTINCT l.id) AS totalLessons
            FROM Course c
            JOIN c.enrollments e
            LEFT JOIN c.modules m
            LEFT JOIN m.lessons l
            LEFT JOIN l.lessonProgresses p
                ON p.enrollment = e
            WHERE c.id = :courseId
              AND e.user.id = :userId
            GROUP BY c.id
        """)
    Optional<ProgressProjection> findProgressByUserId(@Param("userId") Long userId, @Param("courseId") Long courseId);
}
