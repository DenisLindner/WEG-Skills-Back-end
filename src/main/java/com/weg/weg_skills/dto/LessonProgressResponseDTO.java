package com.weg.weg_skills.dto;

import java.time.Instant;

public record LessonProgressResponseDTO(
        Long lessonId,
        Instant completedAt
) {
}
