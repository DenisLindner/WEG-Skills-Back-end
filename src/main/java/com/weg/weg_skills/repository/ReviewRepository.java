package com.weg.weg_skills.repository;

import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Review;
import com.weg.weg_skills.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Boolean existsByCourseAndUser(Course course, User user);
    Boolean existsByIdAndUserId(Long id, Long userId);
    Page<Review> findAllByCourseId(Long courseId, Pageable pageable);
    Page<Review> findAllByUserId(Long userId, Pageable pageable);
}
