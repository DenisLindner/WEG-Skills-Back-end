package com.weg.weg_skills.model;

import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "medias")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bucket;
    @Column(name = "object_key", nullable = false)
    private String objectKey;
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;
    @Column(name = "content_type", nullable = false)
    private String contentType;
    @Column(name = "expected_size", nullable = false)
    private long expectedSize;

    private Long actualSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType;
    @Enumerated(EnumType.STRING)
    @Column(name = "media_status", nullable = false)
    private MediaStatus mediaStatus;

    @ManyToOne
    private User user;

    public Media(String bucket, String objectKey, String originalFilename, String contentType, long expectedSize, MediaType mediaType, MediaStatus mediaStatus, User user) {
        this.bucket = bucket;
        this.objectKey = objectKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.expectedSize = expectedSize;
        this.mediaType = mediaType;
        this.mediaStatus = mediaStatus;
        this.user = user;
    }
}
