package com.weg.weg_skills.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateMediaUploadRequestDTO(
        @NotBlank(message = "Filename is required")
        String fileName,

        @NotBlank(message = "ContentType is required")
        String contentType,

        @NotNull(message = "Size is required")
        @Positive(message = "Size must be positive")
        Long size
) {
}
