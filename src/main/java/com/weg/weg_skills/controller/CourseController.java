package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.service.CourseService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping(path = "/courses")
@AllArgsConstructor
@Tag(name = "Cursos", description = "Endpoints para gerenciamento de cursos")
public class CourseController {
    private CourseService courseService;

    @PostMapping
    @Operation(summary = "Cria um novo curso")
    public ResponseEntity<CourseResponseDTO> create(@RequestBody @Valid CourseCreateRequestDTO dto) {
        return ResponseEntity.status(201).body(courseService.create(dto));
    }

    @PostMapping(path = "/{id}/images/upload")
    @Operation(summary = "Gera ticket para upload de imagem do curso")
    public ResponseEntity<UploadTicketResponseDTO> uploadImage(@PathVariable Long id, @RequestBody @Valid CreateMediaUploadRequestDTO dto) {
        return ResponseEntity.status(201).body(courseService.uploadImage(id, dto));
    }

    @GetMapping
    @Operation(summary = "Lista todos os cursos")
    public ResponseEntity<List<CourseResponseDTO>> findAll() {
        return ResponseEntity.status(200).body(courseService.findAll());
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Busca um curso pelo ID")
    public ResponseEntity<CourseResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.status(200).body(courseService.findById(id));
    }

    @PatchMapping(path = "/{id}")
    @Operation(summary = "Atualiza parcialmente um curso")
    public ResponseEntity<CourseResponseDTO> update(@PathVariable Long id, @RequestBody @Valid CourseUpdateRequestDTO dto) {
        return ResponseEntity.status(200).body(courseService.update(id, dto));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Exclui um curso pelo ID")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        courseService.deleteById(id);
        return ResponseEntity.status(204).build();
    }
}
