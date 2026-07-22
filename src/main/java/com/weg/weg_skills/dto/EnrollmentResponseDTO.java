package com.weg.weg_skills.dto;

import java.time.Instant;

public record EnrollmentResponseDTO(
        Long userId,
        Long courseId,
        Instant enrolledAt
) {
}