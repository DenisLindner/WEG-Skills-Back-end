package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.service.LessonService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping(path = "/lessons")
@AllArgsConstructor
@Tag(name = "Aulas", description = "Endpoints para gerenciamento de aulas")
public class LessonController {
    private LessonService lessonService;

    @PostMapping
    @Operation(summary = "Cria uma nova aula")
    public ResponseEntity<LessonResponseDTO> create(@RequestBody @Valid LessonCreateRequestDTO dto) {
        return ResponseEntity.status(201).body(lessonService.create(dto));
    }

    @PostMapping(path = "/{id}/videos/upload")
    @Operation(summary = "Gera ticket para upload de vídeo da aula")
    public ResponseEntity<UploadTicketResponseDTO> uploadVideo(@PathVariable Long id, @RequestBody @Valid CreateMediaUploadRequestDTO dto) {
        return ResponseEntity.status(201).body(lessonService.uploadVideo(id, dto));
    }

    @GetMapping(path = "/module/{moduleId}")
    @Operation(summary = "Lista todas as aulas de um módulo específico")
    public ResponseEntity<List<LessonResponseDTO>> findAllByModule(@PathVariable Long moduleId) {
        return ResponseEntity.status(200).body(lessonService.findAllByModule(moduleId));
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Busca os detalhes de uma aula pelo ID")
    public ResponseEntity<LessonDetailsResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.status(200).body(lessonService.findById(id));
    }

    @PatchMapping(path = "/{id}")
    @Operation(summary = "Atualiza parcialmente uma aula")
    public ResponseEntity<LessonResponseDTO> update(@PathVariable Long id, @RequestBody @Valid LessonUpdateRequestDTO dto) {
        return ResponseEntity.status(200).body(lessonService.update(id, dto));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Exclui uma aula pelo ID")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        lessonService.deleteById(id);
        return ResponseEntity.status(204).build();
    }
}
