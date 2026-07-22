package com.weg.weg_skills.repository;

import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Enrollment;
import com.weg.weg_skills.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByUserAndCourse(User user, Course course);
    boolean existsByUserIdAndCourse(Long userId, Course course);
    Page<Enrollment> findAllByUser(User user, Pageable pageable);
}