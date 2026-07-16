package com.weg.weg_skills.service;

import com.weg.weg_skills.config.MinioProperties;
import com.weg.weg_skills.repository.MediaRepository;
import com.weg.weg_skills.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MediaService {
    private MediaRepository mediaRepository;
    private UserRepository userRepository;
    private MinioService minioService;
    private MinioProperties minioProperties;
}
