package com.weg.weg_skills.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "enrollments")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Course course;

    public Enrollment(User user, Course course) {
        this.user = user;
        this.course = course;
    }
}