package com.weg.weg_skills.service;

import com.weg.weg_skills.TestData;
import com.weg.weg_skills.dto.CourseCreateRequestDTO;
import com.weg.weg_skills.dto.CourseUpdateRequestDTO;
import com.weg.weg_skills.dto.CreateMediaUploadRequestDTO;
import com.weg.weg_skills.dto.UploadTicketResponseDTO;
import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.DuplicateResourceException;
import com.weg.weg_skills.exceptions.ForbiddenException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.mapper.CourseMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Lesson;
import com.weg.weg_skills.model.Media;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.projection.CourseWithRatingProjection;
import com.weg.weg_skills.repository.CourseRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock CourseRepository courseRepository;
    @Mock MediaService mediaService;
    @Mock UserRepository userRepository;
    @Mock CourseWithRatingProjection courseProjection;

    private CourseService service;

    @BeforeEach
    void setUp() {
        service = new CourseService(courseRepository, new CourseMapper(), mediaService, userRepository);
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
        when(courseRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(course)));
        when(mediaService.getPublicUrl(image)).thenReturn("image-url");

        var response = service.findAll(0, 10);

        assertThat(response.getContent()).singleElement().satisfies(item ->
                assertThat(item.imageUrl()).isEqualTo("image-url"));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(courseRepository).findAll(captor.capture());
        assertThat(Objects.requireNonNull(captor.getValue().getSort().getOrderFor("createdAt")).isDescending()).isTrue();
    }

    @Test
    void shouldFindCoursesContainingNormalizedTitle() {
        Course course = TestData.course(2L, TestData.user(1L, UserRole.INSTRUCTOR));
        when(courseRepository.findAllByTitleContainingIgnoreCase(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(course)));

        var response = service.findAllByTitle(" Java ", 0, 10);

        assertThat(response.getContent()).singleElement().satisfies(item ->
                assertThat(item.title()).isEqualTo("Java Basics"));
        verify(courseRepository).findAllByTitleContainingIgnoreCase("Java", any(Pageable.class));
    }

    @Test
    void shouldListTopThreeCoursesWithRating() {
        when(courseProjection.getId()).thenReturn(2L);
        when(courseProjection.getTitle()).thenReturn("Java Basics");
        when(courseProjection.getDescription()).thenReturn("Course description");
        when(courseProjection.getRating()).thenReturn(9.0);
        when(courseRepository.findMostEnrollmentsCourses(any(Pageable.class)))
                .thenReturn(List.of(courseProjection));

        var response = service.findMostEnrollments();

        assertThat(response).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(2L);
            assertThat(item.rating()).isEqualTo(9.0);
        });
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(courseRepository).findMostEnrollmentsCourses(captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(3);
    }

    @Test
    void shouldFindCourseById() {
        Course course = TestData.course(2L, TestData.user(1L, UserRole.INSTRUCTOR));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));

        assertThat(service.findById(2L).id()).isEqualTo(2L);
    }

    @ParameterizedTest
    @CsvSource({"-1,10", "0,0", "0,101"})
    void shouldRejectInvalidPagination(int page, int size) {
        assertThatThrownBy(() -> service.findAll(page, size)).isInstanceOf(IllegalArgumentException.class);
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

        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(mediaService, never()).getPublicUrl(any(Media.class));
    }
}
