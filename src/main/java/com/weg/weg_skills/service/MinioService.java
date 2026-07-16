package com.weg.weg_skills.service;

import com.weg.weg_skills.config.MinioProperties;
import com.weg.weg_skills.dto.MinioUploadTicketDTO;
import io.minio.MinioClient;
import io.minio.PostPolicy;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class MinioService {
    private MinioClient minioClient;
    private MinioProperties minioProperties;

    public MinioUploadTicketDTO createUploadTicket(
            String bucket,
            String objectKey,
            String contentType,
            long maximumSize
    ) {
        ZonedDateTime expiresAt = ZonedDateTime.now(ZoneOffset.UTC)
                .plus(minioProperties.uploadExpiration());

        try {
            PostPolicy policy = new PostPolicy(bucket, expiresAt);

            policy.addEqualsCondition("key", objectKey);
            policy.addEqualsCondition("Content-Type", contentType);
            policy.addContentLengthRangeCondition(1, maximumSize);

            Map<String, String> fields = new HashMap<>(
                    minioClient.getPresignedPostFormData(policy)
            );

            fields.put("key", objectKey);
            fields.put("Content-Type", contentType);

            return new MinioUploadTicketDTO(
                    buildBucketUrl(bucket),
                    objectKey,
                    fields,
                    expiresAt.toInstant()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public StatObjectResponse getObjectMetadata(
            String bucket,
            String objectKey
    ) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildBucketUrl(String bucket) {
        return removeTrailingSlash(minioProperties.publicEndpoint())
                + "/"
                + bucket;
    }

    private String removeTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }
}
