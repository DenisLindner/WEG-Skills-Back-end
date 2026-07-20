package com.weg.weg_skills.dto;

import java.time.Instant;

public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        Instant expiresAt
) {
}
