package com.weg.weg_skills.dto;

import java.time.LocalDateTime;

public record EnrollmentResponseDTO(
        Long userId,
        Long courseId,
        LocalDateTime enrolledAt
) {
}