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
@Tag(name = "Mídias", description = "Endpoints para gerenciamento de mídias e arquivos")
public class MediaController {
    private MediaService mediaService;

    @PostMapping(path = "/{mediaId}/complete")
    @Operation(summary = "Finaliza e confirma o upload de uma mídia")
    public ResponseEntity<MediaResponseDTO> completeUpload(@PathVariable Long mediaId) {
        return ResponseEntity.status(200).body(mediaService.completeUpload(mediaId));
    }
}
