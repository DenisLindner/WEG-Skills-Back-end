package com.weg.weg_skills.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReviewCreateRequestDTO(
        @NotNull(message = "Rate is required")
        @Min(value = 0, message = "Rate must be less than 10")
        @Max(value = 10, message = "Rate must be greater than 0")
        Integer rate,

        @NotNull(message = "Course ID is required")
        @Positive(message = "Course ID must be a positive number")
        Long courseId,

        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be a positive number")
        Long userId
) {
}
