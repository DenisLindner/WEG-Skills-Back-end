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
import io.minio.StatObjectResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.InvalidPropertiesFormatException;
import java.util.Objects;
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

    @Transactional
    public UploadTicketResponseDTO createModuleImageUpload(
        Long courseId,
        Long moduleId,
        Long userId,
        CreateMediaUploadRequestDTO dto
    ) {
        validateImage(dto);

        String objectKey = String.format(
                "courses/%d/modules/%d/images/%s.%s",
                courseId,
                moduleId,
                UUID.randomUUID(),
                getExtension(dto.contentType())
        );

        return createUpload(
                userId,
                dto,
                MediaType.MODULE_IMAGE,
                minioProperties.buckets().publicAssets(),
                objectKey,
                MAX_IMAGE_SIZE
        );
    }

    @Transactional
    public UploadTicketResponseDTO createLessonVideoUpload(
        Long courseId,
        Long moduleId,
        Long lessonId,
        Long userId,
        CreateMediaUploadRequestDTO dto
    ) {
        validateVideo(dto);

        String objectKey = String.format(
                "courses/%d/modules/%d/lessons/%d/images/%s.%s",
                courseId,
                moduleId,
                lessonId,
                UUID.randomUUID(),
                getExtension(dto.contentType())
        );

        return createUpload(
                userId,
                dto,
                MediaType.LESSON_VIDEO,
                minioProperties.buckets().privateVideos(),
                objectKey,
                MAX_VIDEO_SIZE
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

    @Transactional(noRollbackFor = InvalidPropertiesFormatException.class)
    public Media completeUpload(Long mediaId) throws InvalidPropertiesFormatException {
        Media media = findById(mediaId);

        if (media.isReady()) {
            return media;
        }

        if (!media.isPendingUpload()) {
            throw new IllegalStateException();
        }

        StatObjectResponse metadata =
                minioService.getObjectMetadata(media.getBucket(), media.getObjectKey());

        boolean invalidSize = metadata.size() != media.getExpectedSize();

        boolean invalidType = !Objects.equals(
                metadata.contentType(),
                media.getContentType()
        );

        if (invalidType || invalidSize) {
            media.markAsFailed(metadata.size());

            try {
                minioService.deleteObject(
                        media.getBucket(),
                        media.getObjectKey()
                );
            } catch (RuntimeException exception) {
                log.warn(
                        "Could not delete invalid media object: {}",
                        media.getObjectKey(),
                        exception
                );
            }

            throw new InvalidPropertiesFormatException(
                    "Uploaded file does not match the expected metadata"
            );
        }

        media.markAsReady(metadata.size());

        return media;
    }

    @Transactional(readOnly = true)
    public String getPublicUrl(Long mediaId) {
        Media media = findReadMediaById(mediaId);

        if (media.getMediaType() == MediaType.LESSON_VIDEO) {
            throw new RuntimeException();
        }

        return minioService.createPublicUrl(
                media.getBucket(),
                media.getObjectKey()
        );
    }

    @Transactional(readOnly = true)
    public String getPlaybackVideoUrl(Long mediaId) {
        Media media = findReadMediaById(mediaId);

        if (media.getMediaType() != MediaType.LESSON_VIDEO) {
            throw new RuntimeException();
        }

        return minioService.createPrivateReadUrl(
                media.getBucket(),
                media.getObjectKey()
        );
    }

    @Transactional
    public void delete(Long mediaId) {
        Media media = findById(mediaId);

        if (media.getMediaStatus() != MediaStatus.DELETED) {
            minioService.deleteObject(
                    media.getBucket(),
                    media.getObjectKey()
            );

            media.setMediaStatus(MediaStatus.DELETED);
        }
    }

    private Media findReadMediaById(Long mediaId) {
        Media media = findById(mediaId);

        if (!media.isReady()) {
            throw new RuntimeException();
        }

        return media;
    }

    private Media findById(Long mediaId) {
        return mediaRepository.findById(mediaId).orElseThrow(RuntimeException::new);
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
            case "video/mp4" -> "mp4";
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
