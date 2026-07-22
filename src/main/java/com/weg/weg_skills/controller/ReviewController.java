package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.ReviewCreateRequestDTO;
import com.weg.weg_skills.dto.ReviewResponseDTO;
import com.weg.weg_skills.dto.ReviewUpdateRequestDTO;
import com.weg.weg_skills.service.ReviewService;
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
@RequestMapping(path = "/reviews")
@AllArgsConstructor
@Tag(name = "Reviews", description = "Endpoints for managing course evaluations")
public class ReviewController {
    private ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Creates a new assessment for a course")
    public ResponseEntity<ReviewResponseDTO> create(@RequestBody @Valid ReviewCreateRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(201).body(reviewService.create(dto, jwt.getClaim("userId")));
    }

    @GetMapping(path = "/{courseId}")
    @Operation(summary = "Lists all reviews for a specific course")
    public ResponseEntity<Page<ReviewResponseDTO>> findAllByCourse(@PathVariable Long courseId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(200).body(reviewService.findAllByCourse(courseId, page, size));
    }

    @PatchMapping(path = "/{id}")
    @Operation(summary = "Partially updates an evaluation")
    public ResponseEntity<ReviewResponseDTO> update(@PathVariable Long id, @RequestBody @Valid ReviewUpdateRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(reviewService.update(id, dto, jwt.getClaim("userId")));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Deletes a review by ID")
    public ResponseEntity<Void> deleteById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        reviewService.deleteById(id, jwt.getClaim("userId"));
        return ResponseEntity.status(204).build();
    }
}
