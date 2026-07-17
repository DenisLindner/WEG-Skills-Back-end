package com.weg.weg_skills.dto;

import jakarta.validation.constraints.NotBlank;

public record EnrollmentRequestDTO(
        @NotBlank(message = "User id is required")
        Long userId,

        @NotBlank(message = "course id is required")
        Long courseId
) {
}