package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.CourseCreateRequestDTO;
import com.weg.weg_skills.dto.CourseResponseDTO;
import com.weg.weg_skills.dto.CourseUpdateRequestDTO;
import com.weg.weg_skills.service.CourseService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/courses")
@AllArgsConstructor
public class CourseController {
    private CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseResponseDTO> create(@RequestBody @Valid CourseCreateRequestDTO dto) {
        return ResponseEntity.status(201).body(courseService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<CourseResponseDTO>> findAll() {
        return ResponseEntity.status(200).body(courseService.findAll());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<CourseResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.status(200).body(courseService.findById(id));
    }

    @PatchMapping(path = "/{id}")
    public ResponseEntity<CourseResponseDTO> update(@PathVariable Long id, @RequestBody @Valid CourseUpdateRequestDTO dto) {
        return ResponseEntity.status(200).body(courseService.update(id, dto));
    }
}
