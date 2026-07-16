package com.weg.weg_skills.repository;

import com.weg.weg_skills.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
