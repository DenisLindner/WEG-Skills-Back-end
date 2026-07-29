package com.weg.weg_skills.dto;

import com.weg.weg_skills.enums.CourseStatus;

public record CourseWithRatingResponseDTO(
        Long id,
        String title,
        String description,
        CourseStatus status,
        Double rating,
        String imageUrl
) {
}
