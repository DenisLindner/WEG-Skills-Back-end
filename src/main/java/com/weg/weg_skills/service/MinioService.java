package com.weg.weg_skills.service;

import com.weg.weg_skills.config.MinioProperties;
import io.minio.MinioClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MinioService {
    private MinioClient minioClient;
    private MinioProperties minioProperties;
}
