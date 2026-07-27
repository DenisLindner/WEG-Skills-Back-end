package com.weg.weg_skills.mapper;

import com.weg.weg_skills.TestData;
import com.weg.weg_skills.dto.ReviewCreateRequestDTO;
import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Enrollment;
import com.weg.weg_skills.model.Lesson;
import com.weg.weg_skills.model.Media;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.model.Review;
import com.weg.weg_skills.model.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    @Test
    void shouldMapAuthentication() {
        Instant expiration = Instant.parse("2030-01-01T00:00:00Z");

        var response = new AuthMapper().toResponse("token", "Bearer", expiration);

        assertThat(response.accessToken()).isEqualTo("token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresAt()).isEqualTo(expiration);
    }

    @Test
    void shouldMapUser() {
        UserMapper mapper = new UserMapper();
        User user = mapper.toEntity("User", "user@example.com", "hash", UserRole.STUDENT);
        user.setId(1L);

        var response = mapper.toResponse(user, "picture-url");
        var instructor = mapper.toResponseInstructor(user, "temporary");

        assertThat(user.getRole()).isEqualTo(UserRole.STUDENT);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.pictureUrl()).isEqualTo("picture-url");
        assertThat(instructor.temporaryPassword()).isEqualTo("temporary");
    }

    @Test
    void shouldMapCourseModuleAndLesson() {
        User user = TestData.user(1L, UserRole.INSTRUCTOR);

        Course course = new CourseMapper().toEntity("Course", "Description", user);
        course.setId(2L);
        Module module = new ModuleMapper().toEntity("Module", "Description", course, 1L);
        module.setId(3L);
        Lesson lesson = new LessonMapper().toEntity("Lesson", "Description", module, 1L);
        lesson.setId(4L);

        assertThat(new CourseMapper().toResponse(course, "course-image").imageUrl()).isEqualTo("course-image");
        assertThat(new ModuleMapper().toResponse(module, "module-image").imageUrl()).isEqualTo("module-image");
        assertThat(new ModuleMapper().toResponse(module, "module-image").position()).isEqualTo(1L);
        assertThat(new LessonMapper().toResponse(lesson).id()).isEqualTo(4L);
        assertThat(new LessonMapper().toResponse(lesson).position()).isEqualTo(1L);
        assertThat(new LessonMapper().toResponseDetails(lesson, "video-url").videoUrl()).isEqualTo("video-url");
        assertThat(new LessonMapper().toResponseDetails(lesson, "video-url").position()).isEqualTo(1L);
    }

    @Test
    void shouldMapEnrollmentReviewAndMedia() {
        User user = TestData.user(1L, UserRole.STUDENT);
        Course course = TestData.course(2L, TestData.user(3L, UserRole.INSTRUCTOR));
        Enrollment enrollment = new EnrollmentMapper().toEntity(user, course);
        enrollment.setEnrolledAt(Instant.parse("2030-01-01T00:00:00Z"));

        ReviewMapper reviewMapper = new ReviewMapper();
        Review review = reviewMapper.toEntity(new ReviewCreateRequestDTO(8, 2L), course, user);
        review.setId(4L);
        Media media = TestData.media(5L, user, MediaType.USER_IMAGE, MediaStatus.READY);

        assertThat(new EnrollmentMapper().toResponseDTO(enrollment).courseId()).isEqualTo(2L);
        assertThat(reviewMapper.toResponse(review, "picture").rate()).isEqualTo(8);
        assertThat(new MediaMapper().toResponse(media).mediaStatus()).isEqualTo(MediaStatus.READY);
    }
}
