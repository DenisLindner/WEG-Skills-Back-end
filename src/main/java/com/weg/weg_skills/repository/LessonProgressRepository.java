package com.weg.weg_skills.repository;

import com.weg.weg_skills.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    List<LessonProgress> findAllByEnrollmentUserIdAndLessonModuleCourseId(Long userId, Long courseId);
    LessonProgress findByEnrollmentIdAndLessonId(Long enrollmentId, Long lessonId);
}
