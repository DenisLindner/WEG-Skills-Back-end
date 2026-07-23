package com.weg.weg_skills.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "modules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_module_title_course",
                        columnNames = {"title", "course_id"}
                )
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_media_id", unique = true)
    private Media image;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_image_media_id", unique = true)
    private Media pendingImage;

    @Version
    private int version;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Module(String title, String description, Course course) {
        this.title = title;
        this.description = description;
        this.course = course;
    }
}