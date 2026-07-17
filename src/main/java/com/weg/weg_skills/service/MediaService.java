package com.weg.weg_skills.service;

import com.weg.weg_skills.config.MinioProperties;
import com.weg.weg_skills.dto.CreateMediaUploadRequestDTO;
import com.weg.weg_skills.dto.MinioUploadTicketDTO;
import com.weg.weg_skills.dto.UploadTicketResponseDTO;
import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;
import com.weg.weg_skills.model.Media;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.MediaRepository;
import com.weg.weg_skills.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MediaService {
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4"
    );

    private static final long MAX_IMAGE_SIZE =
            5L * 1024 * 1024;

    private static final long MAX_VIDEO_SIZE =
            2L * 1024 * 1024 * 1024;

    private MediaRepository mediaRepository;
    private UserRepository userRepository;
    private MinioService minioService;
    private MinioProperties minioProperties;

    @Transactional
    public UploadTicketResponseDTO createCourseImageUpload(
        Long courseId,
        Long userId,
        CreateMediaUploadRequestDTO dto
    ) {
        validateImage(dto);

        String objectKey = String.format(
                "courses/%d/images/%s.%s",
                courseId,
                UUID.randomUUID(),
                getExtension(dto.contentType())
        );

        return createUpload(
                userId,
                dto,
                MediaType.COURSE_IMAGE,
                minioProperties.buckets().publicAssets(),
                objectKey,
                MAX_IMAGE_SIZE
        );
    }

    private UploadTicketResponseDTO createUpload(
            Long userId,
            CreateMediaUploadRequestDTO dto,
            MediaType mediaType,
            String bucket,
            String objectKey,
            long maximumSize
    ) {
        User user = userRepository.findById(userId).orElseThrow(RuntimeException::new);

        String originalFilename = sanitizeFilename(dto.fileName());

        Media media = new Media(
                bucket,
                objectKey,
                originalFilename,
                dto.contentType(),
                dto.size(),
                mediaType,
                MediaStatus.PENDING_UPLOAD,
                user
        );

        mediaRepository.save(media);

        MinioUploadTicketDTO ticket = minioService.createUploadTicket(
                bucket,
                objectKey,
                dto.contentType(),
                maximumSize
        );

        return new UploadTicketResponseDTO(
                media.getId(),
                ticket.uploadUrl(),
                objectKey,
                ticket.fields(),
                ticket.expiresAt()
        );
    }

    private void validateImage(CreateMediaUploadRequestDTO dto) {
        validateCommon(dto);

        if (!ALLOWED_IMAGE_TYPES.contains(dto.contentType())) {
            throw new RuntimeException();
        }

        if (dto.size() > MAX_IMAGE_SIZE) {
            throw new RuntimeException();
        }
    }

    private void validateVideo(CreateMediaUploadRequestDTO dto) {
        validateCommon(dto);

        if (!ALLOWED_VIDEO_TYPES.contains(dto.contentType())) {
            throw new RuntimeException();
        }

        if (dto.size() > MAX_VIDEO_SIZE) {
            throw new RuntimeException();
        }
    }

    private void validateCommon(CreateMediaUploadRequestDTO dto) {
        if (dto.size() <= 0) {
            throw new RuntimeException();
        }
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/mp4" -> "mp4";
            default -> throw new RuntimeException();
        };
    }

    private String sanitizeFilename(String filename) {
        String normalized = filename.replace("\\","/");

        int lastSlash = normalized.lastIndexOf("/");

        String cleanName = lastSlash >= 0
                ? normalized.substring(lastSlash + 1)
                : normalized;

        if (cleanName.length() > 255) {
            return cleanName.substring(
                    cleanName.length() - 255
            );
        }

        return cleanName;
    }
}
