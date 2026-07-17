package com.weg.weg_skills.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "courses")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128, unique = true)
    private String title;
    @Column
    private String description;
    @OneToOne
    @JoinColumn(name = "video_media_id")
    private Media video;

    public Course(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
