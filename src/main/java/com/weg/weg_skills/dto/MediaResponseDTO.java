package com.weg.weg_skills.dto;

import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;

public record MediaResponseDTO(
        Long id,
        MediaStatus mediaStatus,
        MediaType mediaType
) {
}
