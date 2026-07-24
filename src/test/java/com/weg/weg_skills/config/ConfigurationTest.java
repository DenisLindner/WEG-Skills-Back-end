package com.weg.weg_skills.config;

import com.weg.weg_skills.config.minio.MinioConfig;
import com.weg.weg_skills.config.minio.MinioProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationTest {

    @Test
    void shouldCreateOpenApiDescription() {
        var openApi = new SwaggerConfig().customOpenAPI();

        assertThat(openApi.getInfo().getTitle()).isEqualTo("WEG Skills");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openApi.getInfo().getDescription()).contains("WEG Skills");
    }

    @Test
    void shouldCreateMinioClient() throws Exception {
        MinioProperties properties = new MinioProperties(
                "http://localhost:9000",
                "http://localhost:9000",
                "access-key",
                "secret-key",
                new MinioProperties.Buckets("public", "private"),
                Duration.ofMinutes(15),
                Duration.ofMinutes(5));

        try (var client = new MinioConfig().minioClient(properties)) {
            assertThat(client).isNotNull();
        }
    }
}
