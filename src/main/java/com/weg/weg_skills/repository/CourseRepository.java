package com.weg.weg_skills.repository;

import com.weg.weg_skills.enums.CourseStatus;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.projection.CourseDetailsProjection;
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

    @Query(value = """
            SELECT c.id AS id, c.title AS title, c.description AS description, c.courseStatus AS courseStatus, COALESCE(AVG(r.rate), 0.0) AS rating, i AS image
            FROM Course c
            LEFT JOIN c.reviews r
            LEFT JOIN c.image i
            WHERE c.courseStatus = :courseStatus
            GROUP BY
                    c.id,
                    c.title,
                    c.description,
                    c.courseStatus,
                    i,
                    c.createdAt
        """, countQuery = """
            SELECT COUNT(c)
            FROM Course c
            WHERE c.courseStatus = :courseStatus
        """)
    Page<CourseWithRatingProjection> findAllByCourseStatus(@Param("courseStatus") CourseStatus courseStatus, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "image")
    @NonNull
    Page<Course> findAll(@NonNull Pageable pageable);

    @EntityGraph(attributePaths = "image")
    Page<Course> findAllByInstructorId(Long instructorId, Pageable pageable);

    @Query(value = """
            SELECT c.id AS id,
                   c.title AS title,
                   c.description AS description,
                   c.courseStatus AS courseStatus,
                   c.instructor.id AS instructorId,
                   COALESCE(AVG(r.rate), 0.0) AS rating,
                   i AS image
            FROM Course c
            LEFT JOIN c.reviews r
            LEFT JOIN c.image i
            WHERE c.id = :id
            GROUP BY
                    c.id,
                    c.title,
                    c.description,
                    c.courseStatus,
                    c.instructor.id,
                    i
        """)
    Optional<CourseDetailsProjection> findByIdWithRating(@Param("id") Long id);
    @Query("""
            SELECT c.id AS id, c.title AS title, c.description AS description, c.courseStatus AS courseStatus, COALESCE(AVG(r.rate), 0.0) AS rating, i AS image
            FROM Course c
            LEFT JOIN c.enrollments e
            LEFT JOIN c.reviews r
            LEFT JOIN c.image i
            WHERE c.courseStatus = :status
            GROUP BY
                    c.id,
                    c.title,
                    c.description,
                    c.courseStatus,
                    i
            ORDER BY COUNT(DISTINCT e.id) DESC
        """)
    List<CourseWithRatingProjection> findMostEnrollmentsCourses(@Param("status") CourseStatus status, Pageable pageable);
    @EntityGraph(attributePaths = "image")
    Page<Course> findAllByInstructorIdAndTitleContainingIgnoreCase(Long instructorId, String title, Pageable pageable);

    @Query(value = """
            SELECT c.id AS id, c.title AS title, c.description AS description, c.courseStatus AS courseStatus, COALESCE(AVG(r.rate), 0.0) AS rating, i AS image
            FROM Course c
            LEFT JOIN c.reviews r
            LEFT JOIN c.image i
            WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))
                    AND c.courseStatus = :courseStatus
            GROUP BY
                    c.id,
                    c.title,
                    c.description,
                    c.courseStatus,
                    i,
                    c.createdAt
        """, countQuery = """
            SELECT COUNT(c)
            FROM Course c
            WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))
              AND c.courseStatus = :courseStatus
        """)
    Page<CourseWithRatingProjection> findAllByTitleContainingIgnoreCaseAndCourseStatus(@Param("title") String title, @Param("courseStatus") CourseStatus courseStatus, Pageable pageable);

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
