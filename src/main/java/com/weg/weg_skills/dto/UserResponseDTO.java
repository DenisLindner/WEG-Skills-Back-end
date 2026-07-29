package com.weg.weg_skills.dto;

import java.time.LocalDate;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        LocalDate birthday,
        String phone,
        String pictureUrl
) {
}
