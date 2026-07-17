package com.weg.weg_skills.dto;

public record LessonDetailsResponseDTO(
        Long id,
        String title,
        String description,
        String videoUrl
) {
}
