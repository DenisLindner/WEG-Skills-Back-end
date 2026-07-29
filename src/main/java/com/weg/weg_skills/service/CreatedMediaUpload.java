package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.UploadTicketResponseDTO;
import com.weg.weg_skills.model.Media;

public record CreatedMediaUpload(
        UploadTicketResponseDTO ticket,
        Media media
) {
}
