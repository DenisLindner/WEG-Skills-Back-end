package com.weg.weg_skills.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RepositionRequestDTO(
        @NotNull(message = "Id is required")
        @Positive(message = "Id must be positive")
        Long parentId,

        @NotNull(message = "Ordered ids list is required")
        @Size(min = 1, max = 100, message = "Ordered ids list size must be between 1 and 100")
        List<
                @NotNull(message = "Id is required")
                @Positive(message = "Id must be positive")
                Long
                > orderedIds
) {
}
