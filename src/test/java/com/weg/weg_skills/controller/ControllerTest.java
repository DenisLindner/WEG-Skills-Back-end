package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.CourseCreateRequestDTO;
import com.weg.weg_skills.dto.CourseUpdateRequestDTO;
import com.weg.weg_skills.dto.CreateMediaUploadRequestDTO;
import com.weg.weg_skills.dto.EnrollmentRequestDTO;
import com.weg.weg_skills.dto.LessonCreateRequestDTO;
import com.weg.weg_skills.dto.LessonUpdateRequestDTO;
import com.weg.weg_skills.dto.LoginRequestDTO;
import com.weg.weg_skills.dto.ModuleCreateRequestDTO;
import com.weg.weg_skills.dto.ModuleUpdateRequestDTO;
import com.weg.weg_skills.dto.PasswordChangeRequestDTO;
import com.weg.weg_skills.dto.RegisterInstructorRequestDTO;
import com.weg.weg_skills.dto.RegisterRequestDTO;
import com.weg.weg_skills.dto.RepositionRequestDTO;
import com.weg.weg_skills.dto.ReviewCreateRequestDTO;
import com.weg.weg_skills.dto.ReviewUpdateRequestDTO;
import com.weg.weg_skills.dto.UserUpdateRequestDTO;
import com.weg.weg_skills.service.CourseService;
import com.weg.weg_skills.service.AuthService;
import com.weg.weg_skills.service.CertificateService;
import com.weg.weg_skills.service.EnrollmentService;
import com.weg.weg_skills.service.LessonService;
import com.weg.weg_skills.service.MediaService;
import com.weg.weg_skills.service.ModuleService;
import com.weg.weg_skills.service.ReviewService;
import com.weg.weg_skills.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ControllerTest {

    @Mock AuthService authService;
    @Mock UserService userService;
    @Mock CourseService courseService;
    @Mock ModuleService moduleService;
    @Mock LessonService lessonService;
    @Mock EnrollmentService enrollmentService;
    @Mock ReviewService reviewService;
    @Mock MediaService mediaService;
    @Mock CertificateService certificateService;

    @Test
    void shouldDelegateAuthenticationEndpoints() {
        AuthController controller = new AuthController(authService);
        var register = new RegisterRequestDTO("User", "user@example.com", "Strong1!");
        var login = new LoginRequestDTO("user@example.com", "Strong1!");

        assertThat(controller.register(register).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.login(login).getStatusCode().value()).isEqualTo(200);

        verify(authService).register(register);
        verify(authService).login(login);
    }

    @Test
    void shouldDelegateUserEndpoints() {
        UserController controller = new UserController(userService);
        Jwt jwt = jwt();
        var instructor = new RegisterInstructorRequestDTO("Instructor", "instructor@example.com");
        var upload = new CreateMediaUploadRequestDTO("image.png", "image/png", 100L);
        var update = new UserUpdateRequestDTO("User", null, null, null, null, null, null);
        var password = new PasswordChangeRequestDTO("NewStrong1!", "current");

        assertThat(controller.getMeProfile(jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.createInstructor(instructor, jwt).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.uploadImage(upload, jwt).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.update(update, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.changePassword(password, jwt).getStatusCode().value()).isEqualTo(204);
        assertThat(controller.delete(jwt).getStatusCode().value()).isEqualTo(204);

        verify(userService).getMeProfile(1L);
        verify(userService).createInstructor(instructor, 1L);
        verify(userService).uploadImage(1L, upload);
        verify(userService).update(1L, update);
        verify(userService).changePassword(1L, password);
        verify(userService).deleteById(1L);
    }

    @Test
    void shouldDelegateCourseEndpoints() {
        CourseController controller = new CourseController(courseService);
        Jwt jwt = jwt();
        var create = new CourseCreateRequestDTO("Course", "Description");
        var upload = new CreateMediaUploadRequestDTO("image.png", "image/png", 100L);
        var update = new CourseUpdateRequestDTO("Updated", null);

        assertThat(controller.create(create, jwt).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.uploadImage(2L, upload, jwt).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.findAll(0, 10, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.findAllAdmin(0, 10, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.findAllPublished(0, 10).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.findAllByTitle("Java", 0, 10, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.findAllByTitlePublished("Java", 0, 10).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.findMostEnrollments().getStatusCode().value()).isEqualTo(200);
        assertThat(controller.findById(2L, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.progressUser(2L, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.generateCertificate(2L, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.update(2L, update, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.publish(2L, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.deleteById(2L, jwt).getStatusCode().value()).isEqualTo(204);

        verify(courseService).create(create, 1L);
        verify(courseService).uploadImage(2L, upload, 1L, List.of("INSTRUCTOR"));
        verify(courseService).findAll(0, 10, 1L);
        verify(courseService).findAllAdmin(0, 10, 1L);
        verify(courseService).findAllPublished(0, 10);
        verify(courseService).findAllByTitle("Java", 0, 10, 1L);
        verify(courseService).findAllByTitlePublished("Java", 0, 10);
        verify(courseService).findMostEnrollments();
        verify(courseService).findById(2L, 1L, List.of("INSTRUCTOR"));
        verify(courseService).findProgressByUser(2L, 1L, List.of("INSTRUCTOR"));
        verify(courseService).createCertificate(2L, 1L, List.of("INSTRUCTOR"));
        verify(courseService).update(2L, update, 1L, List.of("INSTRUCTOR"));
        verify(courseService).publish(2L, 1L, List.of("INSTRUCTOR"));
        verify(courseService).deleteById(2L, 1L, List.of("INSTRUCTOR"));
    }

    @Test
    void shouldDelegateCertificateValidation() {
        CertificateController controller = new CertificateController(certificateService);

        assertThat(controller.validate("certificate-code").getStatusCode().value()).isEqualTo(200);

        verify(certificateService).validateCertificate("certificate-code");
    }

    @Test
    void shouldDelegateModuleEndpoints() {
        ModuleController controller = new ModuleController(moduleService);
        Jwt jwt = jwt();
        var create = new ModuleCreateRequestDTO("Module", "Description", 2L);
        var upload = new CreateMediaUploadRequestDTO("image.png", "image/png", 100L);
        var update = new ModuleUpdateRequestDTO("Updated", null);
        var reposition = new RepositionRequestDTO(2L, List.of(4L, 3L));

        assertThat(controller.create(create, jwt).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.uploadImage(3L, upload, jwt).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.findAllByCourse(2L, 0, 10, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.findById(3L, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.update(3L, update, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.deleteById(3L, jwt).getStatusCode().value()).isEqualTo(204);
        assertThat(controller.reposition(reposition, jwt).getStatusCode().value()).isEqualTo(204);

        verify(moduleService).reposition(reposition, 1L, List.of("INSTRUCTOR"));
    }

    @Test
    void shouldDelegateLessonEndpoints() {
        LessonController controller = new LessonController(lessonService);
        Jwt jwt = jwt();
        var create = new LessonCreateRequestDTO("Lesson", "Description", 3L);
        var upload = new CreateMediaUploadRequestDTO("video.mp4", "video/mp4", 100L);
        var update = new LessonUpdateRequestDTO("Updated", null);
        var reposition = new RepositionRequestDTO(3L, List.of(5L, 4L));

        assertThat(controller.create(create, jwt).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.uploadVideo(4L, upload, jwt).getStatusCode().value()).isEqualTo(201);
        assertThat(controller.findAllByModule(3L, 0, 10, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.findById(4L, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.update(4L, update, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.deleteById(4L, jwt).getStatusCode().value()).isEqualTo(204);
        assertThat(controller.reposition(reposition, jwt).getStatusCode().value()).isEqualTo(204);
        assertThat(controller.completeLesson(4L, jwt).getStatusCode().value()).isEqualTo(200);

        verify(lessonService).reposition(reposition, 1L, List.of("INSTRUCTOR"));
        verify(lessonService).completeLesson(4L, 1L, List.of("INSTRUCTOR"));
    }

    @Test
    void shouldDelegateEnrollmentAndReviewEndpoints() {
        Jwt jwt = jwt();
        EnrollmentController enrollmentController = new EnrollmentController(enrollmentService);
        ReviewController reviewController = new ReviewController(reviewService);
        var enrollment = new EnrollmentRequestDTO(2L);
        var createReview = new ReviewCreateRequestDTO(9, 2L);
        var updateReview = new ReviewUpdateRequestDTO(10);

        assertThat(enrollmentController.enroll(enrollment, jwt).getStatusCode().value()).isEqualTo(201);
        assertThat(enrollmentController.getMeEnrollments(jwt, 0, 10).getStatusCode().value()).isEqualTo(200);
        assertThat(enrollmentController.getMeEnrollmentByCourse(2L, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(reviewController.create(createReview, jwt).getStatusCode().value()).isEqualTo(201);
        assertThat(reviewController.findAllByCourse(2L, 0, 10, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(reviewController.findAllByUser(jwt, 0, 10).getStatusCode().value()).isEqualTo(200);
        assertThat(reviewController.update(5L, updateReview, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(reviewController.deleteById(5L, jwt).getStatusCode().value()).isEqualTo(204);

        verify(enrollmentService).getMeEnrollmentByCourse(2L, 1L);
    }

    @Test
    void shouldDelegateMediaCompletionEndpoints() {
        MediaController controller = new MediaController(mediaService);
        Jwt jwt = jwt();

        assertThat(controller.completeCourseImageUpload(10L, 2L, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.completeModuleImageUpload(11L, 3L, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.completeLessonVideoUpload(12L, 4L, jwt).getStatusCode().value()).isEqualTo(200);
        assertThat(controller.completeUserImageUpload(13L, jwt).getStatusCode().value()).isEqualTo(200);

        verify(mediaService).completeCourseImageUpload(10L, 2L, 1L);
        verify(mediaService).completeModuleImageUpload(11L, 3L, 1L);
        verify(mediaService).completeLessonVideoUpload(12L, 4L, 1L);
        verify(mediaService).completeUserImageUpload(13L, 1L);
    }

    private Jwt jwt() {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("userId", 1L)
                .claim("roles", List.of("INSTRUCTOR"))
                .build();
    }
}
