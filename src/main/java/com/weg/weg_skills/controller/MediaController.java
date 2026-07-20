package com.weg.weg_skills.controller;

import com.weg.weg_skills.dto.MediaResponseDTO;
import com.weg.weg_skills.service.MediaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping(path = "/{mediaId}/complete")
    @Operation(summary = "Finalizes and confirms the media upload")
    public ResponseEntity<MediaResponseDTO> completeUpload(@PathVariable Long mediaId) {
        return ResponseEntity.status(200).body(mediaService.completeUpload(mediaId));
    }
}
