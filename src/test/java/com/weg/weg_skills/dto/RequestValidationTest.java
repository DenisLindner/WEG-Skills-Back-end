package com.weg.weg_skills.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidationTest {

    private static AutoCloseable validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        var factory = Validation.buildDefaultValidatorFactory();
        validatorFactory = factory;
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() throws Exception {
        validatorFactory.close();
    }

    @Test
    void shouldValidateAuthenticationRequests() {
        assertThat(validator.validate(new RegisterRequestDTO("Jo", "invalid", "weak"))).hasSizeGreaterThanOrEqualTo(3);
        assertThat(validator.validate(new RegisterRequestDTO("John", "john@example.com", "Strong1!"))).isEmpty();
        assertThat(validator.validate(new LoginRequestDTO("invalid", ""))).hasSize(2);
    }

    @Test
    void shouldValidateCatalogRequests() {
        assertThat(validator.validate(new CourseCreateRequestDTO("", "x"))).hasSize(3);
        assertThat(validator.validate(new ModuleCreateRequestDTO("Module", "Description", -1L))).hasSize(1);
        assertThat(validator.validate(new LessonCreateRequestDTO("Lesson", "Description", null))).hasSize(1);
        assertThat(validator.validate(new CourseUpdateRequestDTO("ok", null))).hasSize(1);
    }

    @Test
    void shouldValidateRepositionRequest() {
        assertThat(validator.validate(new RepositionRequestDTO(1L, List.of(3L, 2L)))).isEmpty();
        assertThat(validator.validate(new RepositionRequestDTO(0L, List.of()))).hasSize(2);
        assertThat(validator.validate(new RepositionRequestDTO(1L, List.of(-1L)))).hasSize(1);
    }

    @Test
    void shouldValidateMediaEnrollmentAndReviewRequests() {
        assertThat(validator.validate(new CreateMediaUploadRequestDTO("", "", 0L))).hasSize(3);
        assertThat(validator.validate(new EnrollmentRequestDTO(0L))).hasSize(1);
        assertThat(validator.validate(new ReviewCreateRequestDTO(11, null))).hasSize(2);
        assertThat(validator.validate(new ReviewUpdateRequestDTO(-1))).hasSize(1);
    }

    @Test
    void shouldValidateProfileRequests() {
        UserUpdateRequestDTO invalid = new UserUpdateRequestDTO(
                "Jo", "invalid", LocalDate.now().plusDays(1), "123", "x", "y", "z");

        assertThat(validator.validate(invalid)).hasSize(7);
        assertThat(validator.validate(new PasswordChangeRequestDTO("weak", ""))).hasSizeGreaterThanOrEqualTo(3);
        assertThat(validator.validate(new PasswordChangeRequestDTO("Strong1!", "current"))).isEmpty();
    }
}
