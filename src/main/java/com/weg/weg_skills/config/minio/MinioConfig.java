package com.weg.weg_skills.config.minio;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MinioConfig {

    @Bean
    @Primary
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.endpoint())
                .credentials(
                        minioProperties.accessKey(),
                        minioProperties.secretKey()
                )
                .build();
    }

    @Bean
    public MinioClient publicMinioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.publicEndpoint())
                .credentials(
                        minioProperties.accessKey(),
                        minioProperties.secretKey()
                )
                .region("us-east-1")
                .build();
    }
}
