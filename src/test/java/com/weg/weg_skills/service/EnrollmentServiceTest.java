package com.weg.weg_skills.service;

import com.weg.weg_skills.TestData;
import com.weg.weg_skills.dto.EnrollmentRequestDTO;
import com.weg.weg_skills.enums.CourseStatus;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.EnrollmentAlreadyExistsException;
import com.weg.weg_skills.exceptions.EnrollmentNotFoundException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.mapper.EnrollmentMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Enrollment;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.EnrollmentRepository;
import com.weg.weg_skills.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock EnrollmentRepository enrollmentRepository;
    @Mock UserRepository userRepository;
    @Mock CourseRepository courseRepository;

    private EnrollmentService service;

    @BeforeEach
    void setUp() {
        service = new EnrollmentService(enrollmentRepository, new EnrollmentMapper(), userRepository, courseRepository);
    }

    @Test
    void shouldEnrollUser() {
        User user = TestData.user(1L, UserRole.STUDENT);
        Course course = TestData.course(2L, TestData.user(3L, UserRole.INSTRUCTOR));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.save(any())).thenAnswer(invocation -> {
            Enrollment enrollment = invocation.getArgument(0);
            enrollment.setEnrolledAt(Instant.parse("2030-01-01T00:00:00Z"));
            return enrollment;
        });

        var response = service.enrollUser(new EnrollmentRequestDTO(2L), 1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.courseId()).isEqualTo(2L);
    }

    @Test
    void shouldRejectDuplicatedEnrollment() {
        User user = TestData.user(1L, UserRole.STUDENT);
        Course course = TestData.course(2L, TestData.user(3L, UserRole.INSTRUCTOR));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByUserAndCourse(user, course)).thenReturn(true);

        assertThatThrownBy(() -> service.enrollUser(new EnrollmentRequestDTO(2L), 1L))
                .isInstanceOf(EnrollmentAlreadyExistsException.class);
    }

    @Test
    void shouldRejectDraftCourse() {
        Course course = TestData.course(2L, TestData.user(3L, UserRole.INSTRUCTOR));
        course.setCourseStatus(CourseStatus.DRAFT);
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> service.enrollUser(new EnrollmentRequestDTO(2L), 1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldListUserEnrollments() {
        User user = TestData.user(1L, UserRole.STUDENT);
        Course course = TestData.course(2L, TestData.user(3L, UserRole.INSTRUCTOR));
        Enrollment enrollment = new Enrollment(user, course);
        enrollment.setEnrolledAt(Instant.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(enrollmentRepository.findAllByUser(any(), any())).thenReturn(new PageImpl<>(List.of(enrollment)));

        assertThat(service.getMeEnrollments(1L, 0, 10).getContent()).hasSize(1);
    }

    @Test
    void shouldFindCurrentUserEnrollmentByCourse() {
        User user = TestData.user(1L, UserRole.STUDENT);
        Course course = TestData.course(2L, TestData.user(3L, UserRole.INSTRUCTOR));
        Enrollment enrollment = new Enrollment(user, course);
        enrollment.setEnrolledAt(Instant.now());
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(2L)).thenReturn(true);
        when(enrollmentRepository.findByCourseIdAndUserId(2L, 1L)).thenReturn(Optional.of(enrollment));

        var response = service.getMeEnrollmentByCourse(2L, 1L);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.courseId()).isEqualTo(2L);
    }

    @Test
    void shouldRejectMissingCurrentUserEnrollmentByCourse() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courseRepository.existsById(2L)).thenReturn(true);

        assertThatThrownBy(() -> service.getMeEnrollmentByCourse(2L, 1L))
                .isInstanceOf(EnrollmentNotFoundException.class);
    }

    @Test
    void shouldValidateMissingResourcesAndPagination() {
        assertThatThrownBy(() -> service.enrollUser(new EnrollmentRequestDTO(2L), 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.getMeEnrollments(1L, 0, 101))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
