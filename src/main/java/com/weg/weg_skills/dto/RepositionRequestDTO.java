package com.weg.weg_skills.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RepositionRequestDTO(
        @NotNull(message = "Id is required")
        @Positive(message = "Id must be positive")
        Long id,

        @NotNull(message = "Position is required")
        @Positive(message = "Position must be positive")
        Long position
) {
}
