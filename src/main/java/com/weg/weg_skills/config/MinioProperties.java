package com.weg.weg_skills.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
    String endpoint,
    String publicEndpoint,
    String accessKey,
    String secretKey,
    Buckets buckets,
    Duration uploadExpiration,
    Duration playbackExpiration
) {
    public record Buckets(
            String publicAssets,
            String privateVideos
    ) {}
}
