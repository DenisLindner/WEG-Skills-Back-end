package com.weg.weg_skills.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewUpdateRequestDTO(
        @NotNull(message = "Rate is required")
        @Min(value = 0, message = "Rate must be less than 10")
        @Max(value = 10, message = "Rate must be greater than 0")
        Integer rate
) {
}
