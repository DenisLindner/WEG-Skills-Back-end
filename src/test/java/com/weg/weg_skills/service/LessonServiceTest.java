package com.weg.weg_skills.service;

import com.weg.weg_skills.TestData;
import com.weg.weg_skills.dto.CreateMediaUploadRequestDTO;
import com.weg.weg_skills.dto.LessonCreateRequestDTO;
import com.weg.weg_skills.dto.LessonUpdateRequestDTO;
import com.weg.weg_skills.dto.UploadTicketResponseDTO;
import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.DuplicateResourceException;
import com.weg.weg_skills.exceptions.ForbiddenException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.mapper.LessonMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Lesson;
import com.weg.weg_skills.model.Media;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.EnrollmentRepository;
import com.weg.weg_skills.repository.LessonRepository;
import com.weg.weg_skills.repository.ModuleRepository;
import com.weg.weg_skills.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock LessonRepository lessonRepository;
    @Mock ModuleRepository moduleRepository;
    @Mock MediaService mediaService;
    @Mock UserRepository userRepository;
    @Mock EnrollmentRepository enrollmentRepository;

    private LessonService service;

    @BeforeEach
    void setUp() {
        service = new LessonService(lessonRepository, new LessonMapper(), moduleRepository,
                mediaService, userRepository, enrollmentRepository);
    }

    @Test
    void shouldCreateLessonForCourseOwner() {
        Module module = module();
        when(moduleRepository.findById(3L)).thenReturn(Optional.of(module));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson lesson = invocation.getArgument(0);
            lesson.setId(4L);
            return lesson;
        });

        var response = service.create(new LessonCreateRequestDTO(" Lesson ", " Description ", 3L),
                1L, List.of("INSTRUCTOR"));

        assertThat(response.id()).isEqualTo(4L);
        assertThat(response.title()).isEqualTo("Lesson");
    }

    @Test
    void shouldRejectNonOwnerAndDuplicatedLesson() {
        Module module = module();
        when(moduleRepository.findById(3L)).thenReturn(Optional.of(module));

        assertThatThrownBy(() -> service.create(new LessonCreateRequestDTO("Lesson", null, 3L),
                9L, List.of("INSTRUCTOR"))).isInstanceOf(ForbiddenException.class);

        when(lessonRepository.existsByModuleAndTitleIgnoreCase(module, "Lesson")).thenReturn(true);
        assertThatThrownBy(() -> service.create(new LessonCreateRequestDTO("Lesson", null, 3L),
                1L, List.of("INSTRUCTOR"))).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void shouldReplacePendingLessonVideo() {
        Module module = module();
        Lesson lesson = TestData.lesson(4L, module);
        User owner = module.getCourse().getInstructor();
        Media previous = TestData.media(5L, owner, MediaType.LESSON_VIDEO, MediaStatus.PENDING_UPLOAD);
        Media next = TestData.media(6L, owner, MediaType.LESSON_VIDEO, MediaStatus.PENDING_UPLOAD);
        lesson.setPendingVideo(previous);
        UploadTicketResponseDTO ticket = new UploadTicketResponseDTO(6L, "url", "key", Map.of(), Instant.now());
        when(userRepository.existsById(1L)).thenReturn(true);
        when(lessonRepository.findById(4L)).thenReturn(Optional.of(lesson));
        when(mediaService.createLessonVideoUpload(any(), any(), any(), any(), any()))
                .thenReturn(new CreatedMediaUpload(ticket, next));

        assertThat(service.uploadVideo(4L,
                new CreateMediaUploadRequestDTO("video.mp4", "video/mp4", 100L), 1L, List.of("INSTRUCTOR")))
                .isSameAs(ticket);
        assertThat(lesson.getPendingVideo()).isSameAs(next);
        verify(mediaService).delete(5L);
    }

    @Test
    void shouldListLessonsFromExistingModule() {
        Lesson lesson = TestData.lesson(4L, module());
        when(moduleRepository.existsById(3L)).thenReturn(true);
        when(lessonRepository.findAllByModuleId(any(), any())).thenReturn(new PageImpl<>(List.of(lesson)));

        assertThat(service.findAllByModule(3L, 0, 10).getContent())
                .singleElement().satisfies(item -> assertThat(item.id()).isEqualTo(4L));
    }

    @Test
    void shouldAllowEnrolledStudentToReadReadyVideo() {
        Lesson lesson = TestData.lesson(4L, module());
        lesson.setVideo(TestData.media(5L, lesson.getModule().getCourse().getInstructor(),
                MediaType.LESSON_VIDEO, MediaStatus.READY));
        when(userRepository.existsById(10L)).thenReturn(true);
        when(lessonRepository.findById(4L)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.existsByUserIdAndCourse(10L, lesson.getModule().getCourse())).thenReturn(true);
        when(mediaService.getPlaybackVideoUrl(5L)).thenReturn("video-url");

        var response = service.findById(4L, 10L, List.of("STUDENT"));

        assertThat(response.videoUrl()).isEqualTo("video-url");
    }

    @Test
    void shouldAllowOwnerAndAdminButRejectOtherUser() {
        Lesson lesson = TestData.lesson(4L, module());
        when(userRepository.existsById(any())).thenReturn(true);
        when(lessonRepository.findById(4L)).thenReturn(Optional.of(lesson));

        assertThat(service.findById(4L, 1L, List.of("INSTRUCTOR")).id()).isEqualTo(4L);
        assertThat(service.findById(4L, 9L, List.of("ADMIN")).id()).isEqualTo(4L);
        assertThatThrownBy(() -> service.findById(4L, 10L, List.of("STUDENT")))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldUpdateAndDeleteLesson() {
        Lesson lesson = TestData.lesson(4L, module());
        lesson.setVideo(TestData.media(5L, lesson.getModule().getCourse().getInstructor(),
                MediaType.LESSON_VIDEO, MediaStatus.READY));
        when(lessonRepository.findById(4L)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);

        assertThat(service.update(4L, new LessonUpdateRequestDTO(" Updated ", " Description "),
                1L, List.of("INSTRUCTOR")).title()).isEqualTo("Updated");

        service.deleteById(4L, 1L, List.of("INSTRUCTOR"));
        verify(mediaService).delete(5L);
        verify(lessonRepository).delete(lesson);
    }

    @Test
    void shouldFailWhenUserOrModuleDoesNotExist() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> service.findById(4L, 99L, List.of("STUDENT")))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> service.findAllByModule(3L, -1, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Module module() {
        Course course = TestData.course(2L, TestData.user(1L, UserRole.INSTRUCTOR));
        return TestData.module(3L, course);
    }
}
