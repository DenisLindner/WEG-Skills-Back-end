package com.weg.weg_skills.repository;

import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByCreatedAtBeforeAndMediaStatus(Instant limit, MediaStatus mediaStatus);
}
