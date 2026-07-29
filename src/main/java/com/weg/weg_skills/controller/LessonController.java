package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.service.LessonService;
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
@RequestMapping(path = "/lessons")
@AllArgsConstructor
@Tag(name = "Lessons", description = "Endpoints for class management")
public class LessonController {
    private LessonService lessonService;

    @PostMapping
    @Operation(summary = "Create a new class")
    public ResponseEntity<LessonResponseDTO> create(@RequestBody @Valid LessonCreateRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(201).body(lessonService.create(dto, jwt.getClaim("userId"), jwt.getClaim("roles")));
    }

    @PostMapping(path = "/{id}/videos/upload")
    @Operation(summary = "Generates a ticket for uploading the class video")
    public ResponseEntity<UploadTicketResponseDTO> uploadVideo(@PathVariable Long id, @RequestBody @Valid CreateMediaUploadRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(201).body(lessonService.uploadVideo(id, dto, jwt.getClaim("userId"), jwt.getClaim("roles")));
    }

    @GetMapping(path = "/module/{moduleId}")
    @Operation(summary = "Lists all lessons in a specific module")
    public ResponseEntity<Page<LessonResponseDTO>> findAllByModule(@PathVariable Long moduleId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(lessonService.findAllByModule(moduleId, page, size, jwt.getClaim("userId"), jwt.getClaim("roles")));
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Retrieves class details by ID")
    public ResponseEntity<LessonDetailsResponseDTO> findById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(lessonService.findById(id, jwt.getClaim("userId"), jwt.getClaim("roles")));
    }

    @PatchMapping(path = "/{id}")
    @Operation(summary = "Partially updates a class")
    public ResponseEntity<LessonResponseDTO> update(@PathVariable Long id, @RequestBody @Valid LessonUpdateRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(lessonService.update(id, dto, jwt.getClaim("userId"), jwt.getClaim("roles")));
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Deletes a class by ID")
    public ResponseEntity<Void> deleteById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        lessonService.deleteById(id, jwt.getClaim("userId"), jwt.getClaim("roles"));
        return ResponseEntity.status(204).build();
    }

    @PatchMapping(path = "/reposition")
    public ResponseEntity<Void> reposition(@RequestBody @Valid RepositionRequestDTO dto, @AuthenticationPrincipal Jwt jwt) {
        lessonService.reposition(dto, jwt.getClaim("userId"), jwt.getClaim("roles"));
        return ResponseEntity.status(204).build();
    }

    @PutMapping(path = "/{id}/completion")
    @Operation(summary = "Complete lesson route")
    public ResponseEntity<LessonProgressDetailsResponseDTO> completeLesson(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(lessonService.completeLesson(id, jwt.getClaim("userId"), jwt.getClaim("roles")));
    }
}
