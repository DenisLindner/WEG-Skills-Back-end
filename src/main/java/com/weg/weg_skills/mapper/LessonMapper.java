package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.LessonDetailsResponseDTO;
import com.weg.weg_skills.dto.LessonResponseDTO;
import com.weg.weg_skills.model.Lesson;
import com.weg.weg_skills.model.Module;
import org.springframework.stereotype.Component;

@Component
public class LessonMapper {
    public Lesson toEntity(String title, String description, Module module) { return new Lesson(title, description, module); }

    public LessonResponseDTO toResponse(Lesson lesson) {
        return new LessonResponseDTO(lesson.getId(), lesson.getTitle(), lesson.getDescription());
    }

    public LessonDetailsResponseDTO toResponseDetails(Lesson lesson, String url) {
        return new LessonDetailsResponseDTO(lesson.getId(), lesson.getTitle(), lesson.getDescription(), url);
    }
}