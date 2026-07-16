package com.weg.weg_skills.repository;

import com.weg.weg_skills.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Boolean existsByTitleIgnoreCase(String title);
}
