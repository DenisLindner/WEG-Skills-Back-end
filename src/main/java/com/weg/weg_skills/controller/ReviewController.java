package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.ReviewCreateRequestDTO;
import com.weg.weg_skills.dto.ReviewResponseDTO;
import com.weg.weg_skills.dto.ReviewUpdateRequestDTO;
import com.weg.weg_skills.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping(path = "/reviews")
@AllArgsConstructor
@Tag(name = "Avaliações", description = "Endpoints para gerenciamento de avaliações dos cursos")
public class ReviewController {
    private ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Cria uma nova avaliação para um curso")
    public ResponseEntity<ReviewResponseDTO> create(@RequestBody @Valid ReviewCreateRequestDTO dto) {
        return ResponseEntity.status(201).body(reviewService.create(dto));
    }

    @GetMapping(path = "/{courseId}")
    @Operation(summary = "Lista todas as avaliações de um curso específico")
    public ResponseEntity<List<ReviewResponseDTO>> findAllByCourse(@PathVariable Long courseId) {
        return ResponseEntity.status(200).body(reviewService.findAllByCourse(courseId));
    }

    @PatchMapping(path = "/{id}")
    @Operation(summary = "Atualiza parcialmente uma avaliação")
    public ResponseEntity<ReviewResponseDTO> update(@PathVariable Long id, @RequestBody @Valid ReviewUpdateRequestDTO dto) {
        return ResponseEntity.status(200).body(reviewService.update(id, dto));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Exclui uma avaliação pelo ID")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        reviewService.deleteById(id);
        return ResponseEntity.status(204).build();
    }
}
