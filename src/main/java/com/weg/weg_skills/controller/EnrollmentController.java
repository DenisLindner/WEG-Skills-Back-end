package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.EnrollmentRequestDTO;
import com.weg.weg_skills.dto.EnrollmentResponseDTO;
import com.weg.weg_skills.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/enrollments")
@AllArgsConstructor
@Tag(name = "Enrollments", description = "Endpoints for managing course enrollments")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    @PostMapping
    @Operation(summary = "Enrolls a user in a course")
    public ResponseEntity<EnrollmentResponseDTO> enroll(@Valid @RequestBody EnrollmentRequestDTO requestDTO, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(201).body(enrollmentService.enrollUser(requestDTO, jwt.getClaim("userId")));
    }

    @GetMapping(path = "/me")
    @Operation(summary = "Find all enrollments by current user")
    public ResponseEntity<Page<EnrollmentResponseDTO>> getMeEnrollments(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(200).body(enrollmentService.getMeEnrollments(jwt.getClaim("userId"), page, size));
    }
}