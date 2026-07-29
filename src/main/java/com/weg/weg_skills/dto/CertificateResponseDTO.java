package com.weg.weg_skills.dto;

import java.time.Instant;

public record CertificateResponseDTO(
        String code,
        String studentName,
        String courseTitle,
        Long totalLessons,
        Instant endDate
) {
}
