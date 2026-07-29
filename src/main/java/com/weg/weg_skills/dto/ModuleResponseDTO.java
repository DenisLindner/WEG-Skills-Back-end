package com.weg.weg_skills.dto;

public record ModuleResponseDTO(
        Long id,
        String title,
        String description,
        Long position,
        String imageUrl
) {
}
