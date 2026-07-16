package com.weg.weg_skills.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LessonCreateRequestDTO(
        @NotBlank(message = "Title is required")
        @Size(
                min = 3,
                max = 128,
                message = "Title must contain between 3 and 128 characters"
        )
        String title,

        @Size(
                min = 3,
                max = 255,
                message = "Description must contain between 3 and 255 characters"
        )
        String description,

        @NotNull(message = "The module id cannot be null")
        @Positive(message = "The module id has to be positive")
        Long moduleId
) {
}
