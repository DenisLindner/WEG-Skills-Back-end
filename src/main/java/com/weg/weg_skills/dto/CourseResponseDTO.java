package com.weg.weg_skills.dto;

import com.weg.weg_skills.enums.CourseStatus;

public record CourseResponseDTO(
        Long id,
        String title,
        String description,
        CourseStatus status,
        String imageUrl
) {
}
