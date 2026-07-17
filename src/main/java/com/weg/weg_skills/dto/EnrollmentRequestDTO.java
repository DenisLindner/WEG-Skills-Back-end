package com.weg.weg_skills.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EnrollmentRequestDTO(
        @NotNull(message = "User id is required")
        @Positive(message = "User id must be positive")
        Long userId,

        @NotNull(message = "Course id is required")
        @Positive(message = "Course id must be positive")
        Long courseId
) {
}