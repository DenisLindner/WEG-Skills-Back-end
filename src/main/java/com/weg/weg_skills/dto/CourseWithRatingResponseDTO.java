package com.weg.weg_skills.dto;

public record CourseWithRatingResponseDTO(
        Long id,
        String title,
        String description,
        Double rating,
        String imageUrl
) {
}
