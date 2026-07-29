package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.service.ModuleService;
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
@RequestMapping(path = "/modules")
@AllArgsConstructor
@Tag(name = "Modules", description = "Endpoints for managing course modules")
public class ModuleController {
    private ModuleService moduleService;

    @PostMapping
    @Operation(summary = "Creates a new module")
    public ResponseEntity<ModuleResponseDTO> create(@RequestBody @Valid ModuleCreateRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(201).body(moduleService.create(dto, jwt.getClaim("userId"), jwt.getClaim("roles")));
    }

    @PostMapping(path = "/{id}/images/upload")
    @Operation(summary = "Generates a ticket for uploading the module image")
    public ResponseEntity<UploadTicketResponseDTO> uploadImage(@PathVariable Long id, @RequestBody @Valid CreateMediaUploadRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(201).body(moduleService.uploadImage(id, dto, jwt.getClaim("userId"), jwt.getClaim("roles")));
    }

    @GetMapping(path = "/course/{courseId}")
    @Operation(summary = "Lists all modules of a specific course")
    public ResponseEntity<Page<ModuleResponseDTO>> findAllByCourse(@PathVariable Long courseId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(moduleService.findAllByCourse(courseId, page, size, jwt.getClaim("userId"), jwt.getClaim("roles")));
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Searches for a module by ID")
    public ResponseEntity<ModuleResponseDTO> findById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(moduleService.findById(id, jwt.getClaim("userId"), jwt.getClaim("roles")));
    }

    @PatchMapping(path = "/{id}")
    @Operation(summary = "Partially updates a module")
    public ResponseEntity<ModuleResponseDTO> update(@PathVariable Long id, @RequestBody @Valid ModuleUpdateRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(moduleService.update(id, dto, jwt.getClaim("userId"), jwt.getClaim("roles")));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Deletes a module by ID")
    public ResponseEntity<Void> deleteById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        moduleService.deleteById(id, jwt.getClaim("userId"), jwt.getClaim("roles"));
        return ResponseEntity.status(204).build();
    }

    @PatchMapping(path = "/reposition")
    public ResponseEntity<Void> reposition(@RequestBody @Valid RepositionRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        moduleService.reposition(dto, jwt.getClaim("userId"), jwt.getClaim("roles"));
        return ResponseEntity.status(204).build();
    }
}