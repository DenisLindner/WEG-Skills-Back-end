package com.weg.weg_skills.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> resourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<String> duplicateResource(DuplicateResourceException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }

    @ExceptionHandler(EnrollmentAlreadyExistsException.class)
    public ResponseEntity<String> enrollmentAlreadyExists(EnrollmentAlreadyExistsException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }

    @ExceptionHandler(ReviewAlreadyExistsException.class)
    public ResponseEntity<String> reviewAlreadyExists(ReviewAlreadyExistsException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidUploadException.class)
    public ResponseEntity<String> invalidUpload(InvalidUploadException ex) {
        return ResponseEntity.status(422).body(ex.getMessage());
    }

    @ExceptionHandler(MediaMetadataMismatchException.class)
    public ResponseEntity<String> mediaMetadataMismatch(MediaMetadataMismatchException ex) {
        return ResponseEntity.status(422).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidMediaStateException.class)
    public ResponseEntity<String> invalidMediaState(InvalidMediaStateException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }

    @ExceptionHandler(MediaNotReadyException.class)
    public ResponseEntity<String> mediaNotReady(MediaNotReadyException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }

    @ExceptionHandler(StorageServiceException.class)
    public ResponseEntity<String> storageService(StorageServiceException ex) {
        return ResponseEntity.status(503).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidMediaOperationException.class)
    public ResponseEntity<String> invalidMediaOperations(InvalidMediaOperationException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> methodArgumentNotValid(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(400).body(ex.getBindingResult().getFieldErrors().toString());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> httpMessageNotReadable() {
        return ResponseEntity.status(400).body("Malformed or unreadable request body");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> methodArgumentTypeMismatch() {
        return ResponseEntity.status(400).body("Parameter 'id' must be of type Long");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> dataIntegrityViolation() {
        return ResponseEntity.status(409).body("Data integrity conflict");
    }
}
