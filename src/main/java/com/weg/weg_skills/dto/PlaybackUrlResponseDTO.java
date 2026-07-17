package com.weg.weg_skills.dto;

import java.time.Instant;

public record PlaybackUrlResponseDTO(
        String url,
        Instant expiresAt
) {
}
