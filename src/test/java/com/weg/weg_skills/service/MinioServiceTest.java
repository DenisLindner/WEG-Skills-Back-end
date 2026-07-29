package com.weg.weg_skills.service;

import com.weg.weg_skills.config.minio.MinioProperties;
import com.weg.weg_skills.exceptions.StorageServiceException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PostPolicy;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    @Mock MinioClient minioClient;
    @Mock MinioClient publicMinioClient;

    private MinioService service;

    @BeforeEach
    void setUp() {
        MinioProperties properties = new MinioProperties(
                "http://localhost:9000",
                "http://cdn.example.com///",
                "access",
                "secret",
                new MinioProperties.Buckets("public", "private"),
                Duration.ofMinutes(15),
                Duration.ofMinutes(5)
        );
        service = new MinioService(minioClient, publicMinioClient, properties);
    }

    @Test
    void shouldCreateUploadTicket() throws Exception {
        when(minioClient.getPresignedPostFormData(any(PostPolicy.class)))
                .thenReturn(Map.of("policy", "value"));

        var ticket = service.createUploadTicket("public", "images/key.png", "image/png", 1024);

        assertThat(ticket.uploadUrl()).isEqualTo("http://cdn.example.com/public");
        assertThat(ticket.objectKey()).isEqualTo("images/key.png");
        assertThat(ticket.fields())
                .containsEntry("policy", "value")
                .containsEntry("key", "images/key.png")
                .containsEntry("Content-Type", "image/png");
        assertThat(ticket.expiresAt()).isAfter(java.time.Instant.now().plusSeconds(14 * 60));
    }

    @Test
    void shouldReadMetadataAndCreateUrls() throws Exception {
        StatObjectResponse metadata = org.mockito.Mockito.mock(StatObjectResponse.class);
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(metadata);
        when(publicMinioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn("signed-url");

        assertThat(service.getObjectMetadata("private", "video.mp4")).isSameAs(metadata);
        assertThat(service.createPrivateReadUrl("private", "video.mp4")).isEqualTo("signed-url");
        assertThat(service.createPublicUrl("public", "image.png"))
                .isEqualTo("http://cdn.example.com/public/image.png");
    }

    @Test
    void shouldDeleteObject() throws Exception {
        service.deleteObject("public", "image.png");

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void shouldWrapStorageFailures() throws Exception {
        when(minioClient.getPresignedPostFormData(any(PostPolicy.class))).thenThrow(new RuntimeException("down"));
        when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(new RuntimeException("down"));
        when(publicMinioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenThrow(new RuntimeException("down"));
        doThrow(new RuntimeException("down")).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertThatThrownBy(() -> service.createUploadTicket("public", "key", "image/png", 10))
                .isInstanceOf(StorageServiceException.class);
        assertThatThrownBy(() -> service.getObjectMetadata("public", "key"))
                .isInstanceOf(StorageServiceException.class);
        assertThatThrownBy(() -> service.createPrivateReadUrl("private", "key"))
                .isInstanceOf(StorageServiceException.class);
        assertThatThrownBy(() -> service.deleteObject("public", "key"))
                .isInstanceOf(StorageServiceException.class);
    }
}
