package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.AuthResponseDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AuthMapper {
    public AuthResponseDTO toResponse(String accessToken, String tokenType, Instant expiresAt) {
        return new AuthResponseDTO(accessToken, tokenType, expiresAt);
    }
}
