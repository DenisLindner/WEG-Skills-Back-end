package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.LessonProgressDetailsResponseDTO;
import com.weg.weg_skills.dto.LessonProgressResponseDTO;
import com.weg.weg_skills.model.Enrollment;
import com.weg.weg_skills.model.Lesson;
import com.weg.weg_skills.model.LessonProgress;
import org.springframework.stereotype.Component;

@Component
public class LessonProgressMapper {
    public LessonProgress toEntity(Enrollment enrollment, Lesson lesson) {
        return new LessonProgress(enrollment, lesson);
    }

    public LessonProgressResponseDTO toResponse(LessonProgress lessonProgress) {
        return new LessonProgressResponseDTO(lessonProgress.getLesson().getId(), lessonProgress.getCompletedAt());
    }

    public LessonProgressDetailsResponseDTO toResponseDetails(LessonProgress lessonProgress) {
        return new LessonProgressDetailsResponseDTO(lessonProgress.getLesson().getId(), lessonProgress.getEnrollment().getId(), lessonProgress.getCompletedAt());
    }
}
