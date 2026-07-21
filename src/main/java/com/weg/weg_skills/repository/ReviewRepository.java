package com.weg.weg_skills.repository;

import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Review;
import com.weg.weg_skills.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Boolean existsByCourseAndUser(Course course, User user);
    Boolean existsByIdAndUserId(Long id, Long userId);
    Review findByCourseAndUser(Course course, User user);
    List<Review> findAllByCourseId(Long courseId);
}
