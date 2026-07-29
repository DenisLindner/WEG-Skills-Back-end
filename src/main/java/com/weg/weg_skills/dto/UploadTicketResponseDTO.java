package com.weg.weg_skills.dto;

import java.time.Instant;
import java.util.Map;

public record UploadTicketResponseDTO(
        Long mediaId,
        String uploadUrl,
        String objectKey,
        Map<String, String> fields,
        Instant expiresAt
) {
}
