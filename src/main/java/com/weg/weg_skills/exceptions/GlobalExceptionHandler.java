package com.weg.weg_skills.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class, HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail badRequest(RuntimeException ex, HttpServletRequest request) {
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), "bad-request", request.getRequestURI());
    }

    @ExceptionHandler({InvalidCredentialsException.class, UnauthorizedException.class})
    public ProblemDetail unauthorized(RuntimeException ex, HttpServletRequest request) {
        return createProblemDetail(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), "unauthorized", request.getRequestURI());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail forbidden(RuntimeException ex, HttpServletRequest request) {
        return createProblemDetail(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), "forbidden", request.getRequestURI());
    }

    @ExceptionHandler({ResourceNotFoundException.class, UsernameNotFoundException.class})
    public ProblemDetail resourceNotFound(RuntimeException ex, HttpServletRequest request) {
        return createProblemDetail(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), "resource-not-found", request.getRequestURI());
    }

    @ExceptionHandler({DuplicateResourceException.class, UserHasCoursesException.class, EnrollmentAlreadyExistsException.class, ReviewAlreadyExistsException.class,
                        InvalidMediaStateException.class, MediaNotReadyException.class, InvalidMediaOperationException.class})
    public ProblemDetail conflict(RuntimeException ex, HttpServletRequest request) {
        return createProblemDetail(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), "conflict", request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail dataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        return createProblemDetail(HttpStatus.CONFLICT, "Data integrity conflict", "The requested operation conflicts with existing data", "data-integrity-conflict", request.getRequestURI());
    }

    @ExceptionHandler({InvalidUploadException.class, MediaMetadataMismatchException.class})
    public ProblemDetail unprocessableContent(RuntimeException ex, HttpServletRequest request) {
        return createProblemDetail(HttpStatus.UNPROCESSABLE_CONTENT, "Unprocessable Content", ex.getMessage(), "unprocessable-content", request.getRequestURI());
    }

    @ExceptionHandler(StorageServiceException.class)
    public ProblemDetail serviceUnavailable(RuntimeException ex, HttpServletRequest request) {
        return createProblemDetail(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", ex.getMessage(), "service-unavailable", request.getRequestURI());
    }

    private ProblemDetail createProblemDetail(HttpStatusCode status, String title, String detail, String type, String instance) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);

        problemDetail.setTitle(title);
        problemDetail.setType(URI.create("urn:problem:" + type));

        if (instance != null && !instance.isBlank()) {
            problemDetail.setInstance(URI.create(instance));
        }

        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }
}
