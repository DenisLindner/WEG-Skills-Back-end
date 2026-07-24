package com.weg.weg_skills.service;

import com.weg.weg_skills.TestData;
import com.weg.weg_skills.config.minio.MinioProperties;
import com.weg.weg_skills.dto.CreateMediaUploadRequestDTO;
import com.weg.weg_skills.dto.MinioUploadTicketDTO;
import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.ForbiddenException;
import com.weg.weg_skills.exceptions.InvalidMediaOperationException;
import com.weg.weg_skills.exceptions.InvalidMediaStateException;
import com.weg.weg_skills.exceptions.InvalidUploadException;
import com.weg.weg_skills.exceptions.MediaMetadataMismatchException;
import com.weg.weg_skills.exceptions.MediaNotReadyException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.mapper.MediaMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Lesson;
import com.weg.weg_skills.model.Media;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.LessonRepository;
import com.weg.weg_skills.repository.MediaRepository;
import com.weg.weg_skills.repository.ModuleRepository;
import com.weg.weg_skills.repository.UserRepository;
import io.minio.StatObjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE = 2L * 1024 * 1024 * 1024;

    @Mock MediaRepository mediaRepository;
    @Mock UserRepository userRepository;
    @Mock MinioService minioService;
    @Mock CourseRepository courseRepository;
    @Mock ModuleRepository moduleRepository;
    @Mock LessonRepository lessonRepository;

    private MediaService service;

    @BeforeEach
    void setUp() {
        MinioProperties properties = new MinioProperties(
                "http://localhost:9000",
                "http://localhost:9000/",
                "access",
                "secret",
                new MinioProperties.Buckets("public-assets", "private-videos"),
                Duration.ofMinutes(15),
                Duration.ofMinutes(5)
        );
        service = new MediaService(mediaRepository, userRepository, minioService, properties,
                new MediaMapper(), courseRepository, moduleRepository, lessonRepository);
    }

    @ParameterizedTest
    @CsvSource({"image/jpeg,jpg", "image/png,png", "image/webp,webp"})
    void shouldCreateUserImageUpload(String contentType, String extension) {
        User user = TestData.user(1L, UserRole.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> {
            Media media = invocation.getArgument(0);
            media.setId(2L);
            return media;
        });
        when(minioService.createUploadTicket(eq("public-assets"), any(), eq(contentType), eq(MAX_IMAGE_SIZE)))
                .thenAnswer(invocation -> new MinioUploadTicketDTO(
                        "upload-url", invocation.getArgument(1), Map.of("field", "value"), Instant.now()));

        CreatedMediaUpload result = service.createUserImageUpload(1L,
                new CreateMediaUploadRequestDTO("../folder\\picture.png", contentType, MAX_IMAGE_SIZE));

        assertThat(result.ticket().mediaId()).isEqualTo(2L);
        assertThat(result.ticket().objectKey()).startsWith("users/1/images/").endsWith("." + extension);
        assertThat(result.media().getOriginalFilename()).isEqualTo("picture.png");
        assertThat(result.media().getMediaStatus()).isEqualTo(MediaStatus.PENDING_UPLOAD);
        assertThat(result.media().getMediaType()).isEqualTo(MediaType.USER_IMAGE);
    }

    @Test
    void shouldCreateCourseModuleAndLessonObjectKeys() {
        User user = TestData.user(1L, UserRole.INSTRUCTOR);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> {
            Media media = invocation.getArgument(0);
            media.setId(10L);
            return media;
        });
        when(minioService.createUploadTicket(any(), any(), any(), anyLong()))
                .thenAnswer(invocation -> new MinioUploadTicketDTO(
                        "url", invocation.getArgument(1), Map.of(), Instant.now()));

        var course = service.createCourseImageUpload(2L, 1L,
                new CreateMediaUploadRequestDTO("image.png", "image/png", 100L));
        var module = service.createModuleImageUpload(2L, 3L, 1L,
                new CreateMediaUploadRequestDTO("image.webp", "image/webp", 100L));
        var lesson = service.createLessonVideoUpload(2L, 3L, 4L, 1L,
                new CreateMediaUploadRequestDTO("video.mp4", "video/mp4", MAX_VIDEO_SIZE));

        assertThat(course.ticket().objectKey()).startsWith("courses/2/images/");
        assertThat(module.ticket().objectKey()).startsWith("courses/2/modules/3/images/");
        assertThat(lesson.ticket().objectKey()).startsWith("courses/2/modules/3/lessons/4/videos/");
        assertThat(lesson.media().getBucket()).isEqualTo("private-videos");
        assertThat(lesson.media().getMediaType()).isEqualTo(MediaType.LESSON_VIDEO);
    }

    @ParameterizedTest
    @CsvSource({"0,image/png", "-1,image/png", "100,text/plain", "5242881,image/png"})
    void shouldRejectInvalidImage(long size, String contentType) {
        assertThatThrownBy(() -> service.createUserImageUpload(1L,
                new CreateMediaUploadRequestDTO("file", contentType, size)))
                .isInstanceOf(InvalidUploadException.class);

        verify(mediaRepository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidVideoAndMissingOwner() {
        assertThatThrownBy(() -> service.createLessonVideoUpload(1L, 2L, 3L, 4L,
                new CreateMediaUploadRequestDTO("video.mov", "video/quicktime", 100L)))
                .isInstanceOf(InvalidUploadException.class);

        assertThatThrownBy(() -> service.createLessonVideoUpload(1L, 2L, 3L, 4L,
                new CreateMediaUploadRequestDTO("video.mp4", "video/mp4", MAX_VIDEO_SIZE + 1)))
                .isInstanceOf(InvalidUploadException.class);

        when(userRepository.findById(4L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createLessonVideoUpload(1L, 2L, 3L, 4L,
                new CreateMediaUploadRequestDTO("video.mp4", "video/mp4", 100L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldCompleteCourseImageAndDeletePreviousImage() {
        User owner = TestData.user(1L, UserRole.INSTRUCTOR);
        Course course = TestData.course(2L, owner);
        Media previous = TestData.media(3L, owner, MediaType.COURSE_IMAGE, MediaStatus.READY);
        Media pending = TestData.media(4L, owner, MediaType.COURSE_IMAGE, MediaStatus.PENDING_UPLOAD);
        course.setImage(previous);
        course.setPendingImage(pending);
        StatObjectResponse metadata = metadata(100L, "image/png");
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(minioService.getObjectMetadata("public-assets", "objects/key")).thenReturn(metadata);
        when(mediaRepository.findById(3L)).thenReturn(Optional.of(previous));

        var response = service.completeCourseImageUpload(4L, 2L, 1L);

        assertThat(response.mediaStatus()).isEqualTo(MediaStatus.READY);
        assertThat(course.getImage()).isSameAs(pending);
        assertThat(course.getPendingImage()).isNull();
        verify(courseRepository).saveAndFlush(course);
        verify(minioService).deleteObject(previous.getBucket(), previous.getObjectKey());
        verify(mediaRepository).delete(previous);
    }

    @Test
    void shouldReturnAlreadyCompletedCourseImage() {
        User owner = TestData.user(1L, UserRole.INSTRUCTOR);
        Course course = TestData.course(2L, owner);
        course.setImage(TestData.media(4L, owner, MediaType.COURSE_IMAGE, MediaStatus.READY));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));

        assertThat(service.completeCourseImageUpload(4L, 2L, 1L).id()).isEqualTo(4L);
        verify(minioService, never()).getObjectMetadata(any(), any());
    }

    @Test
    void shouldCompleteModuleLessonAndUserUploads() {
        User owner = TestData.user(1L, UserRole.INSTRUCTOR);
        Course course = TestData.course(2L, owner);
        Module module = TestData.module(3L, course);
        Lesson lesson = TestData.lesson(4L, module);
        Media moduleImage = TestData.media(5L, owner, MediaType.MODULE_IMAGE, MediaStatus.PENDING_UPLOAD);
        Media video = TestData.media(6L, owner, MediaType.LESSON_VIDEO, MediaStatus.PENDING_UPLOAD);
        Media userImage = TestData.media(7L, owner, MediaType.USER_IMAGE, MediaStatus.PENDING_UPLOAD);
        module.setPendingImage(moduleImage);
        lesson.setPendingVideo(video);
        owner.setPendingImage(userImage);
        when(moduleRepository.findById(3L)).thenReturn(Optional.of(module));
        when(lessonRepository.findById(4L)).thenReturn(Optional.of(lesson));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        StatObjectResponse imageMetadata = metadata(100L, "image/png");
        StatObjectResponse videoMetadata = metadata(100L, "video/mp4");
        when(minioService.getObjectMetadata("public-assets", "objects/key")).thenReturn(imageMetadata);
        when(minioService.getObjectMetadata("private-videos", "objects/key")).thenReturn(videoMetadata);

        assertThat(service.completeModuleImageUpload(5L, 3L, 1L).mediaStatus()).isEqualTo(MediaStatus.READY);
        assertThat(service.completeLessonVideoUpload(6L, 4L, 1L).mediaStatus()).isEqualTo(MediaStatus.READY);
        assertThat(service.completeUserImageUpload(7L, 1L).mediaStatus()).isEqualTo(MediaStatus.READY);
        assertThat(module.getPendingImage()).isNull();
        assertThat(lesson.getPendingVideo()).isNull();
        assertThat(owner.getPendingImage()).isNull();
    }

    @Test
    void shouldRejectWrongPendingMediaOwnerAndState() {
        User owner = TestData.user(1L, UserRole.INSTRUCTOR);
        Course course = TestData.course(2L, owner);
        Media pending = TestData.media(4L, owner, MediaType.COURSE_IMAGE, MediaStatus.PENDING_UPLOAD);
        course.setPendingImage(pending);
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.completeCourseImageUpload(99L, 2L, 1L))
                .isInstanceOf(InvalidMediaOperationException.class);
        assertThatThrownBy(() -> service.completeCourseImageUpload(4L, 2L, 9L))
                .isInstanceOf(ForbiddenException.class);

        pending.setMediaStatus(MediaStatus.FAILED);
        assertThatThrownBy(() -> service.completeCourseImageUpload(4L, 2L, 1L))
                .isInstanceOf(InvalidMediaStateException.class);
    }

    @Test
    void shouldFailAndDeleteObjectWhenMetadataDoesNotMatch() {
        User owner = TestData.user(1L, UserRole.STUDENT);
        Media pending = TestData.media(2L, owner, MediaType.USER_IMAGE, MediaStatus.PENDING_UPLOAD);
        owner.setPendingImage(pending);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        StatObjectResponse invalidMetadata = metadata(99L, "image/jpeg");
        when(minioService.getObjectMetadata("public-assets", "objects/key"))
                .thenReturn(invalidMetadata);

        assertThatThrownBy(() -> service.completeUserImageUpload(2L, 1L))
                .isInstanceOf(MediaMetadataMismatchException.class);

        assertThat(pending.getMediaStatus()).isEqualTo(MediaStatus.FAILED);
        assertThat(pending.getActualSize()).isEqualTo(99L);
        assertThat(owner.getPendingImage()).isNull();
        verify(minioService).deleteObject("public-assets", "objects/key");
    }

    @Test
    void shouldCreatePublicAndPrivateUrlsOnlyForReadyMatchingMedia() {
        User owner = TestData.user(1L, UserRole.STUDENT);
        Media image = TestData.media(2L, owner, MediaType.USER_IMAGE, MediaStatus.READY);
        Media video = TestData.media(3L, owner, MediaType.LESSON_VIDEO, MediaStatus.READY);
        when(mediaRepository.findById(2L)).thenReturn(Optional.of(image));
        when(mediaRepository.findById(3L)).thenReturn(Optional.of(video));
        when(minioService.createPublicUrl(image.getBucket(), image.getObjectKey())).thenReturn("public-url");
        when(minioService.createPrivateReadUrl(video.getBucket(), video.getObjectKey())).thenReturn("private-url");

        assertThat(service.getPublicUrl(2L)).isEqualTo("public-url");
        assertThat(service.getPlaybackVideoUrl(3L)).isEqualTo("private-url");
        assertThatThrownBy(() -> service.getPublicUrl(3L)).isInstanceOf(InvalidMediaOperationException.class);
        assertThatThrownBy(() -> service.getPlaybackVideoUrl(2L)).isInstanceOf(InvalidMediaOperationException.class);

        image.setMediaStatus(MediaStatus.PENDING_UPLOAD);
        assertThatThrownBy(() -> service.getPublicUrl(2L)).isInstanceOf(MediaNotReadyException.class);
    }

    @Test
    void shouldDeleteMediaAndIgnoreAlreadyDeletedMedia() {
        User owner = TestData.user(1L, UserRole.STUDENT);
        Media ready = TestData.media(2L, owner, MediaType.USER_IMAGE, MediaStatus.READY);
        Media deleted = TestData.media(3L, owner, MediaType.USER_IMAGE, MediaStatus.DELETED);
        when(mediaRepository.findById(2L)).thenReturn(Optional.of(ready));
        when(mediaRepository.findById(3L)).thenReturn(Optional.of(deleted));

        service.delete(2L);
        service.delete(3L);

        verify(minioService).deleteObject(ready.getBucket(), ready.getObjectKey());
        verify(mediaRepository).delete(ready);
        verify(mediaRepository, never()).delete(deleted);
    }

    @Test
    void shouldDeleteAllExpiredPendingUploads() {
        User owner = TestData.user(1L, UserRole.STUDENT);
        Media first = TestData.media(2L, owner, MediaType.USER_IMAGE, MediaStatus.PENDING_UPLOAD);
        Media second = TestData.media(3L, owner, MediaType.USER_IMAGE, MediaStatus.PENDING_UPLOAD);
        when(mediaRepository.findByCreatedAtBeforeAndMediaStatus(any(), eq(MediaStatus.PENDING_UPLOAD)))
                .thenReturn(List.of(first, second));
        when(mediaRepository.findById(2L)).thenReturn(Optional.of(first));
        when(mediaRepository.findById(3L)).thenReturn(Optional.of(second));

        service.expirePendingUploads();

        verify(mediaRepository).delete(first);
        verify(mediaRepository).delete(second);
    }

    private StatObjectResponse metadata(long size, String contentType) {
        StatObjectResponse metadata = org.mockito.Mockito.mock(StatObjectResponse.class);
        when(metadata.size()).thenReturn(size);
        when(metadata.contentType()).thenReturn(contentType);
        return metadata;
    }
}
