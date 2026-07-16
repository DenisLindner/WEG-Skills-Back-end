package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.ReviewCreateRequestDTO;
import com.weg.weg_skills.dto.ReviewResponseDTO;
import com.weg.weg_skills.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/reviews")
@AllArgsConstructor
public class ReviewController {
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> create(@RequestBody @Valid ReviewCreateRequestDTO dto) {
        return ResponseEntity.status(201).body(reviewService.create(dto));
    }

    @GetMapping(path = "/{courseId}")
    public ResponseEntity<List<ReviewResponseDTO>> findAllByCourse(@PathVariable Long courseId) {
        return ResponseEntity.status(200).body(reviewService.findAllByCourse(courseId));
    }
}
