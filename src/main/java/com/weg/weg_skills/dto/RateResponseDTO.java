package com.weg.weg_skills.dto;

public record RateResponseDTO(
        Long id,
        Integer rate,
        String courseTitle,
        String userName,
        String userPictureUrl
) {
}
