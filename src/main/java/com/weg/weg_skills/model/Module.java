package com.weg.weg_skills.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "modules")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128, unique = true)
    private String title;
    @Column
    private String description;
    @ManyToOne
    private Course course;
    @OneToOne
    @JoinColumn(name = "image_media_id")
    private Media image;

    public Module(String title, String description, Course course) {
        this.title = title;
        this.description = description;
        this.course = course;
    }
}