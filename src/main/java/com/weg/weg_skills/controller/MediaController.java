package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.MediaResponseDTO;
import com.weg.weg_skills.service.MediaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(path = "/medias")
@AllArgsConstructor
@Tag(name = "Medias", description = "Endpoints for media and file management")
public class MediaController {
    private MediaService mediaService;

    @PostMapping(path = "/{mediaId}/course/{courseId}/complete")
    @Operation(summary = "Finalizes and confirms the course image upload")
    public ResponseEntity<MediaResponseDTO> completeCourseImageUpload(@PathVariable Long mediaId, @PathVariable Long courseId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(mediaService.completeCourseImageUpload(mediaId, courseId, jwt.getClaim("userId")));
    }

    @PostMapping(path = "/{mediaId}/module/{moduleId}/complete")
    @Operation(summary = "Finalizes and confirms the module image upload")
    public ResponseEntity<MediaResponseDTO> completeModuleImageUpload(@PathVariable Long mediaId, @PathVariable Long moduleId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(mediaService.completeModuleImageUpload(mediaId, moduleId, jwt.getClaim("userId")));
    }

    @PostMapping(path = "/{mediaId}/lesson/{lessonId}/complete")
    @Operation(summary = "Finalizes and confirms the lesson video upload")
    public ResponseEntity<MediaResponseDTO> completeLessonVideoUpload(@PathVariable Long mediaId, @PathVariable Long lessonId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(mediaService.completeLessonVideoUpload(mediaId, lessonId, jwt.getClaim("userId")));
    }

    @PostMapping(path = "/{mediaId}/me/complete")
    @Operation(summary = "Finalizes and confirms the user image upload")
    public ResponseEntity<MediaResponseDTO> completeUserImageUpload(@PathVariable Long mediaId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(200).body(mediaService.completeUserImageUpload(mediaId, jwt.getClaim("userId")));
    }
}
