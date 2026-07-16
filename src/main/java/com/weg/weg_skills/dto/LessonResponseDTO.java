package com.weg.weg_skills.dto;

public record LessonResponseDTO(
        Long id,
        String title,
        String description,
        String videoUrl
) {
}
