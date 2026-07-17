package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.*;
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

    @PostMapping(path = "/{id}/images/upload")
    public ResponseEntity<UploadTicketResponseDTO> uploadImage(@PathVariable Long id, @RequestBody @Valid CreateMediaUploadRequestDTO dto) {
        return ResponseEntity.status(201).body(moduleService.uploadImage(id, dto));
    }

    @GetMapping(path = "/course/{courseId}")
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