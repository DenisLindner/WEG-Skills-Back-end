package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.MediaResponseDTO;
import com.weg.weg_skills.model.Media;
import org.springframework.stereotype.Component;

@Component
public class MediaMapper {
    public MediaResponseDTO toResponse(Media media) {
        return new MediaResponseDTO(media.getId(), media.getMediaStatus(), media.getMediaType());
    }
}
