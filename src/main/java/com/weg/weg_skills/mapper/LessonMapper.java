package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.LessonCreateRequestDTO;
import com.weg.weg_skills.dto.LessonResponseDTO;
import com.weg.weg_skills.model.Lesson;
import org.springframework.stereotype.Component;

@Component
public class LessonMapper {
    public Lesson toEntity(LessonCreateRequestDTO dto) { return new Lesson(dto.title(), dto.description()); }

    public LessonResponseDTO toResponse(Lesson lesson) {
        return new LessonResponseDTO(lesson.getId(), lesson.getTitle(), lesson.getDescription(), lesson.getVideoUrl());
    }
}