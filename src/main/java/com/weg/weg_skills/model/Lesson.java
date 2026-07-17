package com.weg.weg_skills.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lessons")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128, unique = true)
    private String title;
    @Column
    private String description;
    @ManyToOne
    private Module module;
    @OneToOne
    @JoinColumn(name = "video_media_id")
    private Media video;

    public Lesson(String title, String description, Module module) {
        this.title = title;
        this.description = description;
        this.module = module;
    }
}
