package com.weg.weg_skills.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "cors.config")
public record CorsProperties(
        List<String> origins
) {
}
