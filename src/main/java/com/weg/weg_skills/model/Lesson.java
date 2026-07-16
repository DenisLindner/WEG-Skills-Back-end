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

    @Column(name = "video_url")
    private String videoUrl;

    @ManyToOne
    private Module module;

    public Lesson(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
