package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.ModuleCreateRequestDTO;
import com.weg.weg_skills.dto.ModuleResponseDTO;
import com.weg.weg_skills.dto.ModuleUpdateRequestDTO;
import com.weg.weg_skills.service.ModuleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/modules")
@AllArgsConstructor
public class ModuleController {
    private ModuleService moduleService;

    @PostMapping
    public ResponseEntity<ModuleResponseDTO> create(@RequestBody @Valid ModuleCreateRequestDTO dto) {
        return ResponseEntity.status(201).body(moduleService.create(dto));
    }

    @GetMapping(path = "/{courseId}")
    public ResponseEntity<List<ModuleResponseDTO>> findAll(@PathVariable Long courseId) {
        return ResponseEntity.status(200).body(moduleService.findAllByCourse(courseId));
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ModuleResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.status(200).body(moduleService.findById(id));
    }

    @PatchMapping(path = "/{id}")
    public ResponseEntity<ModuleResponseDTO> update(@PathVariable Long id, @RequestBody @Valid ModuleUpdateRequestDTO dto) {
        return ResponseEntity.status(200).body(moduleService.update(id, dto));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        moduleService.deleteById(id);
        return ResponseEntity.status(204).build();
    }
}