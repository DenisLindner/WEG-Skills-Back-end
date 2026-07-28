package com.weg.weg_skills.config;

import com.weg.weg_skills.config.minio.MinioConfig;
import com.weg.weg_skills.config.minio.MinioProperties;
import com.weg.weg_skills.dto.CourseWithRatingResponseDTO;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

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

    @Test
    void shouldSerializeTopCoursesCacheAsJson() {
        var configuration = new CacheConfig().redisCacheConfiguration();
        var courses = List.of(new CourseWithRatingResponseDTO(
                1L,
                "Java",
                "Course description",
                9.5,
                "http://localhost/image.png"
        ));

        var serialized = configuration.getValueSerializationPair().write(courses);
        Object deserialized = configuration.getValueSerializationPair().read(serialized);

        assertThat(deserialized).isEqualTo(courses);
        assertThat(configuration.getKeyPrefixFor("topCourses"))
                .isEqualTo("weg-skills:v1:topCourses::");
        assertThat(configuration.getTtlFunction().getTimeToLive("key", courses))
                .isEqualTo(Duration.ofMinutes(2));
    }
}
