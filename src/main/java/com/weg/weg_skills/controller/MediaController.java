package com.weg.weg_skills.controller;

import com.weg.weg_skills.model.Media;
import com.weg.weg_skills.service.MediaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.InvalidPropertiesFormatException;

@RestController
@RequestMapping(path = "medias")
@AllArgsConstructor
public class MediaController {
    private MediaService mediaService;

    @PostMapping(path = "/{mediaId}/complete")
    public ResponseEntity<Media> completeUpload(@PathVariable Long mediaId) throws InvalidPropertiesFormatException {
        return ResponseEntity.status(200).body(mediaService.completeUpload(mediaId));
    }
}
