package com.weg.weg_skills.dto;

import java.time.Instant;

public record LessonProgressDetailsResponseDTO(
        Long lessonId,
        Long enrollmentId,
        Instant completedAt
) {
}
