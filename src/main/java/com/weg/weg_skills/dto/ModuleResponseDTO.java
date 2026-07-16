package com.weg.weg_skills.dto;

import com.weg.weg_skills.model.Course;

public record ModuleResponseDTO(
        Long id,
        String title,
        String description,
        String imageUrl,
        Course course
) {
}
