package com.weg.weg_skills.service;

import com.weg.weg_skills.config.minio.MinioProperties;
import com.weg.weg_skills.dto.CreateMediaUploadRequestDTO;
import com.weg.weg_skills.dto.MediaResponseDTO;
import com.weg.weg_skills.dto.MinioUploadTicketDTO;
import com.weg.weg_skills.dto.UploadTicketResponseDTO;
import com.weg.weg_skills.enums.InvalidUpload;
import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;
import com.weg.weg_skills.exceptions.*;
import com.weg.weg_skills.mapper.MediaMapper;
import com.weg.weg_skills.model.Media;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.MediaRepository;
import com.weg.weg_skills.repository.UserRepository;
import io.minio.StatObjectResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private MediaMapper mediaMapper;

    @Transactional
    public CreatedMediaUpload createCourseImageUpload(
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
    public CreatedMediaUpload createModuleImageUpload(
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
    public CreatedMediaUpload createUserImageUpload(
        Long userId,
        CreateMediaUploadRequestDTO dto
    ) {
        validateImage(dto);

        String objectKey = String.format(
                "users/%d/images/%s.%s",
                userId,
                UUID.randomUUID(),
                getExtension(dto.contentType())
        );

        return createUpload(
                userId,
                dto,
                MediaType.USER_IMAGE,
                minioProperties.buckets().publicAssets(),
                objectKey,
                MAX_IMAGE_SIZE
        );
    }

    @Transactional
    public CreatedMediaUpload createLessonVideoUpload(
        Long courseId,
        Long moduleId,
        Long lessonId,
        Long userId,
        CreateMediaUploadRequestDTO dto
    ) {
        validateVideo(dto);

        String objectKey = String.format(
                "courses/%d/modules/%d/lessons/%d/videos/%s.%s",
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

    private CreatedMediaUpload createUpload(
            Long userId,
            CreateMediaUploadRequestDTO dto,
            MediaType mediaType,
            String bucket,
            String objectKey,
            long maximumSize
    ) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));

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

        media = mediaRepository.save(media);

        MinioUploadTicketDTO ticket = minioService.createUploadTicket(
                bucket,
                objectKey,
                dto.contentType(),
                maximumSize
        );

        return new CreatedMediaUpload(
                new UploadTicketResponseDTO(
                    media.getId(),
                    ticket.uploadUrl(),
                    objectKey,
                    ticket.fields(),
                    ticket.expiresAt()
                ),
                media
        );
    }

    @Transactional(noRollbackFor = MediaMetadataMismatchException.class)
    public MediaResponseDTO completeUpload(Long mediaId, Long userId) {
        Media media = findById(mediaId);

        if (!media.getUser().getId().equals(userId)) {
            throw new ForbiddenException();
        }

        if (media.isReady()) {
            return mediaMapper.toResponse(media);
        }

        if (!media.isPendingUpload()) {
            throw new InvalidMediaStateException(media.getMediaStatus());
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

            throw new MediaMetadataMismatchException();
        }

        media.markAsReady(metadata.size());

        return mediaMapper.toResponse(media);
    }

    @Transactional(readOnly = true)
    public String getPublicUrl(Long mediaId) {
        Media media = findReadMediaById(mediaId);

        if (media.getMediaType() == MediaType.LESSON_VIDEO) {
            throw new InvalidMediaOperationException();
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
            throw new InvalidMediaOperationException();
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
            throw new MediaNotReadyException();
        }

        return media;
    }

    private Media findById(Long mediaId) {
        return mediaRepository.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("Media", mediaId));
    }

    private void validateImage(CreateMediaUploadRequestDTO dto) {
        validateCommon(dto);

        if (!ALLOWED_IMAGE_TYPES.contains(dto.contentType())) {
            throw new InvalidUploadException(InvalidUpload.UNSUPPORTED_CONTENT_TYPE);
        }

        if (dto.size() > MAX_IMAGE_SIZE) {
            throw new InvalidUploadException(InvalidUpload.FILE_TOO_LARGE);
        }
    }

    private void validateVideo(CreateMediaUploadRequestDTO dto) {
        validateCommon(dto);

        if (!ALLOWED_VIDEO_TYPES.contains(dto.contentType())) {
            throw new InvalidUploadException(InvalidUpload.UNSUPPORTED_CONTENT_TYPE);
        }

        if (dto.size() > MAX_VIDEO_SIZE) {
            throw new InvalidUploadException(InvalidUpload.FILE_TOO_LARGE);
        }
    }

    private void validateCommon(CreateMediaUploadRequestDTO dto) {
        if (dto.size() <= 0) {
            throw new InvalidUploadException(InvalidUpload.INVALID_FILE_SIZE);
        }
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "video/mp4" -> "mp4";
            default -> throw new InvalidUploadException(InvalidUpload.UNSUPPORTED_CONTENT_TYPE);
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
