package com.weg.weg_skills.service;

import com.weg.weg_skills.TestData;
import com.weg.weg_skills.dto.ReviewCreateRequestDTO;
import com.weg.weg_skills.dto.ReviewUpdateRequestDTO;
import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.ForbiddenException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.exceptions.ReviewAlreadyExistsException;
import com.weg.weg_skills.mapper.ReviewMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Review;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.EnrollmentRepository;
import com.weg.weg_skills.repository.ReviewRepository;
import com.weg.weg_skills.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock ReviewRepository reviewRepository;
    @Mock CourseRepository courseRepository;
    @Mock UserRepository userRepository;
    @Mock EnrollmentRepository enrollmentRepository;
    @Mock MediaService mediaService;

    private ReviewService service;

    @BeforeEach
    void setUp() {
        service = new ReviewService(reviewRepository, new ReviewMapper(), courseRepository,
                userRepository, enrollmentRepository, mediaService);
    }

    @Test
    void shouldCreateReviewForEnrolledUser() {
        User user = student();
        user.setImage(TestData.media(3L, user, MediaType.USER_IMAGE, MediaStatus.READY));
        Course course = course();
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(enrollmentRepository.existsByUserAndCourse(user, course)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(4L);
            return review;
        });
        when(mediaService.getPublicUrl(user.getImage())).thenReturn("picture-url");

        var response = service.create(new ReviewCreateRequestDTO(9, 2L), 1L);

        assertThat(response.id()).isEqualTo(4L);
        assertThat(response.rate()).isEqualTo(9);
        assertThat(response.userPictureUrl()).isEqualTo("picture-url");
    }

    @Test
    void shouldRejectReviewWhenNotEnrolledOrAlreadyExists() {
        User user = student();
        Course course = course();
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.create(new ReviewCreateRequestDTO(9, 2L), 1L))
                .isInstanceOf(ForbiddenException.class);

        when(enrollmentRepository.existsByUserAndCourse(user, course)).thenReturn(true);
        when(reviewRepository.existsByCourseAndUser(course, user)).thenReturn(true);
        assertThatThrownBy(() -> service.create(new ReviewCreateRequestDTO(9, 2L), 1L))
                .isInstanceOf(ReviewAlreadyExistsException.class);
    }

    @Test
    void shouldListReviewsByCourseAndUser() {
        Course course = course();
        Review review = new Review(8, course, student());
        review.setId(3L);
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findAllByCourseId(any(), any())).thenReturn(new PageImpl<>(List.of(review)));
        when(reviewRepository.findAllByUserId(any(), any())).thenReturn(new PageImpl<>(List.of(review)));

        assertThat(service.findAllByCourse(2L, 0, 10, 1L, List.of("STUDENT")).getContent()).hasSize(1);
        assertThat(service.findAllByUser(1L, 0, 10).getContent()).hasSize(1);
    }

    @Test
    void shouldUpdateOnlyReviewOwner() {
        Review review = new Review(7, course(), student());
        review.setId(3L);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(review);

        assertThat(service.update(3L, new ReviewUpdateRequestDTO(10), 1L).rate()).isEqualTo(10);

        assertThatThrownBy(() -> service.update(3L, new ReviewUpdateRequestDTO(5), 9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldDeleteOnlyReviewOwner() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.existsById(3L)).thenReturn(true);
        when(reviewRepository.existsByIdAndUserId(3L, 1L)).thenReturn(true);

        service.deleteById(3L, 1L);

        verify(reviewRepository).deleteById(3L);
    }

    @Test
    void shouldRejectInvalidPaginationOrMissingParent() {
        assertThatThrownBy(() -> service.findAllByCourse(2L, -1, 10, 1L, List.of("STUDENT")))
                .isInstanceOf(IllegalArgumentException.class);
        when(courseRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findAllByCourse(2L, 0, 10, 1L, List.of("STUDENT")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private User student() {
        return TestData.user(1L, UserRole.STUDENT);
    }

    private Course course() {
        return TestData.course(2L, TestData.user(5L, UserRole.INSTRUCTOR));
    }
}
