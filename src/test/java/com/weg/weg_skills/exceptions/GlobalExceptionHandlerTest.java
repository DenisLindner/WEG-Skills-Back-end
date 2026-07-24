package com.weg.weg_skills.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/courses/99");

    @Test
    void shouldCreateBadRequestProblem() {
        var problem = handler.badRequest(new IllegalArgumentException("invalid page"), request);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Bad Request");
        assertThat(problem.getDetail()).isEqualTo("invalid page");
        assertThat(Objects.requireNonNull(problem.getType()).toString()).isEqualTo("urn:problem:bad-request");
        assertThat(Objects.requireNonNull(problem.getInstance()).toString()).isEqualTo("/courses/99");
        assertThat(problem.getProperties()).containsKey("timestamp");
    }

    @Test
    void shouldMapApplicationExceptions() {
        assertThat(handler.unauthorized(new InvalidCredentialsException(), request).getStatus()).isEqualTo(401);
        assertThat(handler.forbidden(new ForbiddenException(), request).getStatus()).isEqualTo(403);
        assertThat(handler.resourceNotFound(new ResourceNotFoundException("Course", 99L), request).getStatus()).isEqualTo(404);
        assertThat(handler.conflict(new DuplicateResourceException("Course", "title", "Java"), request).getStatus()).isEqualTo(409);
        assertThat(handler.unprocessableContent(new InvalidUploadException(
                com.weg.weg_skills.enums.InvalidUpload.FILE_TOO_LARGE), request).getStatus()).isEqualTo(422);
        assertThat(handler.serviceUnavailable(new StorageServiceException("storage unavailable", new RuntimeException()), request).getStatus()).isEqualTo(503);
    }

    @Test
    void shouldHideDatabaseDetails() {
        var problem = handler.dataIntegrityViolation(request);

        assertThat(problem.getStatus()).isEqualTo(409);
        assertThat(problem.getDetail()).isEqualTo("The requested operation conflicts with existing data");
    }
}
