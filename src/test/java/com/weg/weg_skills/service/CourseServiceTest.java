package com.weg.weg_skills.service;

import com.weg.weg_skills.TestData;
import com.weg.weg_skills.dto.CourseCreateRequestDTO;
import com.weg.weg_skills.dto.CourseUpdateRequestDTO;
import com.weg.weg_skills.dto.CreateMediaUploadRequestDTO;
import com.weg.weg_skills.dto.UploadTicketResponseDTO;
import com.weg.weg_skills.enums.CourseStatus;
import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.DuplicateResourceException;
import com.weg.weg_skills.exceptions.ForbiddenException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.mapper.CertificateMapper;
import com.weg.weg_skills.mapper.CourseMapper;
import com.weg.weg_skills.mapper.LessonProgressMapper;
import com.weg.weg_skills.model.Certificate;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Enrollment;
import com.weg.weg_skills.model.Lesson;
import com.weg.weg_skills.model.LessonProgress;
import com.weg.weg_skills.model.Media;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.projection.CourseWithRatingProjection;
import com.weg.weg_skills.projection.ProgressProjection;
import com.weg.weg_skills.repository.CertificateRepository;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.EnrollmentRepository;
import com.weg.weg_skills.repository.LessonProgressRepository;
import com.weg.weg_skills.repository.LessonRepository;
import com.weg.weg_skills.repository.ModuleRepository;
import com.weg.weg_skills.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock CourseRepository courseRepository;
    @Mock MediaService mediaService;
    @Mock UserRepository userRepository;
    @Mock LessonProgressRepository lessonProgressRepository;
    @Mock EnrollmentRepository enrollmentRepository;
    @Mock CertificateRepository certificateRepository;
    @Mock ModuleRepository moduleRepository;
    @Mock LessonRepository lessonRepository;
    @Mock CourseWithRatingProjection courseProjection;
    @Mock ProgressProjection progressProjection;

    private CourseService service;

    @BeforeEach
    void setUp() {
        service = new CourseService(moduleRepository, lessonRepository, courseRepository, new CourseMapper(), mediaService, userRepository,
                lessonProgressRepository, new LessonProgressMapper(), enrollmentRepository,
                certificateRepository, new CertificateMapper());
    }

    @Test
    void shouldCreateCourseForInstructor() {
        User instructor = TestData.user(1L, UserRole.INSTRUCTOR);
        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(10L);
            return course;
        });

        var response = service.create(new CourseCreateRequestDTO(" Java Basics ", " Description "), 1L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Java Basics");
        assertThat(response.description()).isEqualTo("Description");
        assertThat(response.status()).isEqualTo(CourseStatus.DRAFT);
    }

    @Test
    void shouldRejectStudentAndDuplicatedCourse() {
        User user = TestData.user(1L, UserRole.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.create(new CourseCreateRequestDTO("Course", null), 1L))
                .isInstanceOf(ForbiddenException.class);

        user.setRole(UserRole.INSTRUCTOR);
        when(courseRepository.existsByTitleIgnoreCase("Course")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CourseCreateRequestDTO("Course", null), 1L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void shouldReplacePendingCourseImage() {
        User instructor = TestData.user(1L, UserRole.INSTRUCTOR);
        Course course = TestData.course(2L, instructor);
        Media previous = TestData.media(3L, instructor, MediaType.COURSE_IMAGE, MediaStatus.PENDING_UPLOAD);
        Media next = TestData.media(4L, instructor, MediaType.COURSE_IMAGE, MediaStatus.PENDING_UPLOAD);
        course.setPendingImage(previous);
        UploadTicketResponseDTO ticket = new UploadTicketResponseDTO(4L, "url", "key", Map.of(), Instant.now());
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(mediaService.createCourseImageUpload(any(), any(), any())).thenReturn(new CreatedMediaUpload(ticket, next));

        assertThat(service.uploadImage(2L,
                new CreateMediaUploadRequestDTO("image.png", "image/png", 100L), 1L, List.of("INSTRUCTOR")))
                .isSameAs(ticket);
        assertThat(course.getPendingImage()).isSameAs(next);
        verify(mediaService).delete(3L);
    }

    @Test
    void shouldListCoursesWithReadyImages() {
        User instructor = TestData.user(1L, UserRole.INSTRUCTOR);
        Course course = TestData.course(2L, instructor);
        Media image = TestData.media(3L, instructor, MediaType.COURSE_IMAGE, MediaStatus.READY);
        course.setImage(image);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.findAllByInstructorId(eq(1L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(course)));
        when(mediaService.getPublicUrl(image)).thenReturn("image-url");

        var response = service.findAll(0, 10, 1L);

        assertThat(response.getContent()).singleElement().satisfies(item ->
                assertThat(item.imageUrl()).isEqualTo("image-url"));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(courseRepository).findAllByInstructorId(eq(1L), captor.capture());
        assertThat(Objects.requireNonNull(captor.getValue().getSort().getOrderFor("createdAt")).isDescending()).isTrue();
    }

    @Test
    void shouldFindCoursesContainingNormalizedTitle() {
        Course course = TestData.course(2L, TestData.user(1L, UserRole.INSTRUCTOR));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.findAllByInstructorIdAndTitleContainingIgnoreCase(eq(1L), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(course)));

        var response = service.findAllByTitle(" Java ", 0, 10, 1L);

        assertThat(response.getContent()).singleElement().satisfies(item ->
                assertThat(item.title()).isEqualTo("Java Basics"));
        verify(courseRepository).findAllByInstructorIdAndTitleContainingIgnoreCase(eq(1L), eq("Java"), any(Pageable.class));
    }

    @Test
    void shouldListTopThreeCoursesWithRating() {
        when(courseProjection.getId()).thenReturn(2L);
        when(courseProjection.getTitle()).thenReturn("Java Basics");
        when(courseProjection.getDescription()).thenReturn("Course description");
        when(courseProjection.getCourseStatus()).thenReturn(CourseStatus.PUBLISHED);
        when(courseProjection.getRating()).thenReturn(9.0);
        when(courseRepository.findMostEnrollmentsCourses(eq(CourseStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(List.of(courseProjection));

        var response = service.findMostEnrollments();

        assertThat(response).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(2L);
            assertThat(item.rating()).isEqualTo(9.0);
        });
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(courseRepository).findMostEnrollmentsCourses(eq(CourseStatus.PUBLISHED), captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(3);
    }

    @Test
    void shouldFindCourseById() {
        Course course = TestData.course(2L, TestData.user(1L, UserRole.INSTRUCTOR));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));

        assertThat(service.findById(2L, 5L, List.of("STUDENT")).id()).isEqualTo(2L);
    }

    @Test
    void shouldFindCourseProgress() {
        User student = TestData.user(1L, UserRole.STUDENT);
        Course course = TestData.course(2L, TestData.user(3L, UserRole.INSTRUCTOR));
        Module module = TestData.module(4L, course);
        Lesson lesson = TestData.lesson(5L, module);
        Enrollment enrollment = new Enrollment(student, course);
        enrollment.setId(6L);
        LessonProgress lessonProgress = new LessonProgress(enrollment, lesson);
        lessonProgress.setCompletedAt(Instant.now());

        when(userRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByCourseIdAndUserId(2L, 1L)).thenReturn(true);
        when(courseRepository.findProgressByUserId(1L, 2L)).thenReturn(Optional.of(progressProjection));
        when(progressProjection.getCourseId()).thenReturn(2L);
        when(progressProjection.getCompletedLessons()).thenReturn(1L);
        when(progressProjection.getTotalLessons()).thenReturn(2L);
        when(progressProjection.getPercentage()).thenReturn(50.0);
        when(lessonProgressRepository.findAllByEnrollmentUserIdAndLessonModuleCourseId(1L, 2L))
                .thenReturn(List.of(lessonProgress));

        var response = service.findProgressByUser(2L, 1L, List.of("STUDENT"));

        assertThat(response.percentage()).isEqualTo(50.0);
        assertThat(response.lessons()).singleElement().satisfies(item ->
                assertThat(item.lessonId()).isEqualTo(5L));
    }

    @Test
    void shouldCreateCertificateForCompletedCourse() {
        User student = TestData.user(1L, UserRole.STUDENT);
        Course course = TestData.course(2L, TestData.user(3L, UserRole.INSTRUCTOR));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByCourseIdAndUserId(2L, 1L)).thenReturn(true);
        when(courseRepository.findProgressByUserId(1L, 2L)).thenReturn(Optional.of(progressProjection));
        when(progressProjection.getTotalLessons()).thenReturn(2L);
        when(progressProjection.getCompletedLessons()).thenReturn(2L);
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> {
            Certificate certificate = invocation.getArgument(0);
            certificate.setCompletedAt(Instant.now());
            return certificate;
        });

        var response = service.createCertificate(2L, 1L, List.of("STUDENT"));

        assertThat(response.studentName()).isEqualTo("Test User");
        assertThat(response.courseTitle()).isEqualTo("Java Basics");
        assertThat(response.totalLessons()).isEqualTo(2L);
        assertThat(response.code()).isNotBlank();
        verify(certificateRepository).save(any(Certificate.class));
    }

    @Test
    void shouldReturnExistingCertificate() {
        User student = TestData.user(1L, UserRole.STUDENT);
        Course course = TestData.course(2L, TestData.user(3L, UserRole.INSTRUCTOR));
        Certificate certificate = new Certificate("existing-code", student.getName(), course.getTitle(), 2L, student, course);
        certificate.setCompletedAt(Instant.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByCourseIdAndUserId(2L, 1L)).thenReturn(true);
        when(certificateRepository.findByCourseIdAndUserId(2L, 1L)).thenReturn(certificate);

        var response = service.createCertificate(2L, 1L, List.of("STUDENT"));

        assertThat(response.code()).isEqualTo("existing-code");
        verify(certificateRepository, never()).save(any(Certificate.class));
    }

    @Test
    void shouldRejectCertificateForEmptyOrIncompleteCourse() {
        User student = TestData.user(1L, UserRole.STUDENT);
        Course course = TestData.course(2L, TestData.user(3L, UserRole.INSTRUCTOR));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByCourseIdAndUserId(2L, 1L)).thenReturn(true);
        when(courseRepository.findProgressByUserId(1L, 2L)).thenReturn(Optional.of(progressProjection));
        when(progressProjection.getTotalLessons()).thenReturn(0L);

        assertThatThrownBy(() -> service.createCertificate(2L, 1L, List.of("STUDENT")))
                .isInstanceOf(IllegalStateException.class);

        when(progressProjection.getTotalLessons()).thenReturn(2L);
        when(progressProjection.getCompletedLessons()).thenReturn(1L);

        assertThatThrownBy(() -> service.createCertificate(2L, 1L, List.of("STUDENT")))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @CsvSource({"-1,10", "0,0", "0,101"})
    void shouldRejectInvalidPagination(int page, int size) {
        assertThatThrownBy(() -> service.findAll(page, size, 1L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAllowAdminToUpdateAnotherInstructorCourse() {
        User instructor = TestData.user(1L, UserRole.INSTRUCTOR);
        Course course = TestData.course(2L, instructor);
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(courseRepository.save(course)).thenReturn(course);

        var response = service.update(2L, new CourseUpdateRequestDTO(" New Title ", " New Description "),
                99L, List.of("ADMIN"));

        assertThat(response.title()).isEqualTo("New Title");
        assertThat(response.description()).isEqualTo("New Description");
    }

    @Test
    void shouldRejectUpdateByAnotherInstructor() {
        Course course = TestData.course(2L, TestData.user(1L, UserRole.INSTRUCTOR));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.update(2L, new CourseUpdateRequestDTO("Title", null),
                99L, List.of("INSTRUCTOR"))).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldDeleteCourseAndNestedMedia() {
        User instructor = TestData.user(1L, UserRole.INSTRUCTOR);
        Course course = TestData.course(2L, instructor);
        course.setImage(TestData.media(3L, instructor, MediaType.COURSE_IMAGE, MediaStatus.READY));
        Module module = TestData.module(4L, course);
        module.setImage(TestData.media(5L, instructor, MediaType.MODULE_IMAGE, MediaStatus.READY));
        Lesson lesson = TestData.lesson(6L, module);
        lesson.setVideo(TestData.media(7L, instructor, MediaType.LESSON_VIDEO, MediaStatus.READY));
        module.getLessons().add(lesson);
        course.getModules().add(module);
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));

        service.deleteById(2L, 1L, List.of("INSTRUCTOR"));

        verify(mediaService).delete(3L);
        verify(mediaService).delete(5L);
        verify(mediaService).delete(7L);
        verify(courseRepository).delete(course);
    }

    @Test
    void shouldFailWhenCourseDoesNotExist() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L, 1L, List.of("INSTRUCTOR"))).isInstanceOf(ResourceNotFoundException.class);
        verify(mediaService, never()).getPublicUrl(any(Media.class));
    }

    @Test
    void shouldPublishCompleteCourse() {
        User instructor = TestData.user(1L, UserRole.INSTRUCTOR);
        Course course = TestData.course(2L, instructor);
        course.setCourseStatus(CourseStatus.DRAFT);
        course.setImage(TestData.media(3L, instructor, MediaType.COURSE_IMAGE, MediaStatus.READY));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(moduleRepository.countByCourseId(2L)).thenReturn(1L);
        when(lessonRepository.countByModuleCourseId(2L)).thenReturn(2L);
        when(lessonRepository.countByModuleCourseIdAndVideoIsNotNull(2L)).thenReturn(2L);
        when(courseRepository.save(course)).thenReturn(course);

        var response = service.publish(2L, 1L, List.of("INSTRUCTOR"));

        assertThat(response.status()).isEqualTo(CourseStatus.PUBLISHED);
        verify(courseRepository).save(course);
    }

    @Test
    void shouldRejectPublishingIncompleteCourse() {
        Course course = TestData.course(2L, TestData.user(1L, UserRole.INSTRUCTOR));
        course.setCourseStatus(CourseStatus.DRAFT);
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.publish(2L, 1L, List.of("INSTRUCTOR")))
                .isInstanceOf(IllegalStateException.class);
        verify(courseRepository, never()).save(course);
    }

    @Test
    void shouldHideDraftFromAnotherUser() {
        Course course = TestData.course(2L, TestData.user(1L, UserRole.INSTRUCTOR));
        course.setCourseStatus(CourseStatus.DRAFT);
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.findById(2L, 9L, List.of("STUDENT")))
                .isInstanceOf(ForbiddenException.class);
    }
}
