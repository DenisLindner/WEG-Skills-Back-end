package com.weg.weg_skills.integration;

import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Enrollment;
import com.weg.weg_skills.model.Lesson;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.model.Media;
import com.weg.weg_skills.model.Review;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.EnrollmentRepository;
import com.weg.weg_skills.repository.LessonRepository;
import com.weg.weg_skills.repository.ModuleRepository;
import com.weg.weg_skills.repository.MediaRepository;
import com.weg.weg_skills.repository.ReviewRepository;
import com.weg.weg_skills.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:repository_tests;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class RepositoryIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired CourseRepository courseRepository;
    @Autowired ModuleRepository moduleRepository;
    @Autowired LessonRepository lessonRepository;
    @Autowired EnrollmentRepository enrollmentRepository;
    @Autowired ReviewRepository reviewRepository;
    @Autowired MediaRepository mediaRepository;

    @Test
    void shouldPersistAndQueryLearningFlow() {
        User instructor = userRepository.save(new User(
                "Instructor", "instructor@example.com", "password", UserRole.INSTRUCTOR));
        User student = userRepository.save(new User(
                "Student", "student@example.com", "password", UserRole.STUDENT));
        Course course = courseRepository.save(new Course("Java", "Description", instructor));
        Module module = moduleRepository.save(new Module("Basics", "Description", course, 1L));
        Lesson lesson = lessonRepository.save(new Lesson("Introduction", "Description", module, 1L));
        Enrollment enrollment = enrollmentRepository.save(new Enrollment(student, course));
        Review review = reviewRepository.save(new Review(9, course, student));
        Media media = mediaRepository.save(new Media("public", "users/image.png", "image.png", "image/png",
                100L, MediaType.USER_IMAGE, MediaStatus.PENDING_UPLOAD, student));
        enrollmentRepository.flush();

        assertThat(userRepository.existsByEmailIgnoreCase("INSTRUCTOR@EXAMPLE.COM")).isTrue();
        assertThat(userRepository.findByEmailIgnoreCase("STUDENT@EXAMPLE.COM")).contains(student);
        assertThat(courseRepository.existsByTitleIgnoreCase("java")).isTrue();
        assertThat(courseRepository.findAllByTitleContainingIgnoreCase("jav", PageRequest.of(0, 10)))
                .contains(course);
        assertThat(moduleRepository.existsByCourseAndTitleIgnoreCase(course, "BASICS")).isTrue();
        assertThat(lessonRepository.existsByModuleAndTitleIgnoreCase(module, "INTRODUCTION")).isTrue();
        assertThat(module.getPosition()).isEqualTo(1L);
        assertThat(lesson.getPosition()).isEqualTo(1L);
        assertThat(enrollmentRepository.existsByUserAndCourse(student, course)).isTrue();
        assertThat(enrollmentRepository.existsByUserIdAndCourse(student.getId(), course)).isTrue();
        assertThat(reviewRepository.existsByCourseAndUser(course, student)).isTrue();
        assertThat(reviewRepository.existsByIdAndUserId(review.getId(), student.getId())).isTrue();
        assertThat(moduleRepository.findAllByCourseId(course.getId(), PageRequest.of(0, 10))).contains(module);
        assertThat(lessonRepository.findAllByModuleId(module.getId(), PageRequest.of(0, 10))).contains(lesson);
        assertThat(enrollmentRepository.findAllByUser(student, PageRequest.of(0, 10))).contains(enrollment);
        assertThat(reviewRepository.findAllByCourseId(course.getId(), PageRequest.of(0, 10))).contains(review);
        assertThat(reviewRepository.findAllByUserId(student.getId(), PageRequest.of(0, 10))).contains(review);
        assertThat(mediaRepository.findByCreatedAtBeforeAndMediaStatus(
                Instant.now().plusSeconds(1), MediaStatus.PENDING_UPLOAD)).contains(media);
        assertThat(courseRepository.findMostEnrollmentsCourses(PageRequest.of(0, 3)))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getTitle()).isEqualTo("Java");
                    assertThat(item.getRating()).isEqualTo(9.0);
                    assertThat(item.getImage()).isNull();
                });
    }
}
