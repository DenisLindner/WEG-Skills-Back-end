package com.weg.weg_skills.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "lessons",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_lesson_module_title",
                        columnNames = {"module_id", "title"}
                ),
                @UniqueConstraint(
                        name = "uk_lesson_module_position",
                        columnNames = {"module_id", "position"}
                )
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_media_id", unique = true)
    private Media video;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_video_media_id", unique = true)
    private Media pendingVideo;

    @Version
    private int version;

    @Column(nullable = false)
    private Long position;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Lesson(String title, String description, Module module, Long position) {
        this.title = title;
        this.description = description;
        this.module = module;
        this.position = position;
    }
}
