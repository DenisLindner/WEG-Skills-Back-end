package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.LessonCreateRequestDTO;
import com.weg.weg_skills.dto.LessonResponseDTO;
import com.weg.weg_skills.dto.LessonUpdateRequestDTO;
import com.weg.weg_skills.service.LessonService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/lessons")
@AllArgsConstructor
public class LessonController {
    private LessonService lessonService;

    @PostMapping
    public ResponseEntity<LessonResponseDTO> create(@RequestBody @Valid LessonCreateRequestDTO dto) {
        return ResponseEntity.status(201).body(lessonService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<LessonResponseDTO>> findAll() {
        return ResponseEntity.status(200).body(lessonService.findAll());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<LessonResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.status(200).body(lessonService.findById(id));
    }

    @PatchMapping(path = "/{id}")
    public ResponseEntity<LessonResponseDTO> update(@PathVariable Long id, @Valid LessonUpdateRequestDTO dto) {
        return ResponseEntity.status(200).body(lessonService.update(id, dto));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        lessonService.deleteById(id);
        return ResponseEntity.status(204).build();
    }
}
