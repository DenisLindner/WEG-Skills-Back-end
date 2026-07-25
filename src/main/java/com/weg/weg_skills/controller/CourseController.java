package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.service.CourseService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping(path = "/courses")
@AllArgsConstructor
@Tag(name = "Courses", description = "Endpoints for course management")
public class CourseController {
    private CourseService courseService;

    @PostMapping
    @Operation(summary = "Create a new course")
    public ResponseEntity<CourseResponseDTO> create(@RequestBody @Valid CourseCreateRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(201).body(courseService.create(dto, jwt.getClaim("userId")));
    }

    @PostMapping(path = "/{id}/images/upload")
    @Operation(summary = "Generates a ticket for uploading the course image")
    public ResponseEntity<UploadTicketResponseDTO> uploadImage(@PathVariable Long id, @RequestBody @Valid CreateMediaUploadRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(201).body(courseService.uploadImage(id, dto, jwt.getClaim("userId"), jwt.getClaim("roles")));
    }

    @GetMapping
    @Operation(summary = "Lists all courses")
    public ResponseEntity<Page<CourseResponseDTO>> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(200).body(courseService.findAll(page, size));
    }

    @GetMapping(path = "/title")
    @Operation(summary = "Lists all courses containing title")
    public ResponseEntity<Page<CourseResponseDTO>> findAllByTitle(@RequestParam String title, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(200).body(courseService.findAllByTitle(title, page, size));
    }

    @GetMapping(path = "/top-courses")
    @Operation(summary = "Lists top courses with most enrollments")
    public ResponseEntity<List<CourseWithRatingResponseDTO>> findMostEnrollments() {
        return ResponseEntity.status(200).body(courseService.findMostEnrollments());
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Search for a course by ID")
    public ResponseEntity<CourseResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.status(200).body(courseService.findById(id));
    }

    @PatchMapping(path = "/{id}")
    @Operation(summary = "Partially updates a course")
    public ResponseEntity<CourseResponseDTO> update(@PathVariable Long id, @RequestBody @Valid CourseUpdateRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(courseService.update(id, dto, jwt.getClaim("userId"), jwt.getClaim("roles")));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Deletes a course by ID")
    public ResponseEntity<Void> deleteById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        courseService.deleteById(id, jwt.getClaim("userId"), jwt.getClaim("roles"));
        return ResponseEntity.status(204).build();
    }
}
