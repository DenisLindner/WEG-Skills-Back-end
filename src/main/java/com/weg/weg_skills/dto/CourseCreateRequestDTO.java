package com.weg.weg_skills.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseCreateRequestDTO(
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
        String description
) {
}
