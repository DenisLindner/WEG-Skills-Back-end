package com.weg.weg_skills.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "certificates")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Certificate {
    @Id
    @Column(unique = true, length = 50, nullable = false)
    private String code;

    @Column(name = "student_name", nullable = false)
    private String studentName;
    @Column(name = "course_title", nullable = false)
    private String courseTitle;
    @Column(name = "total_lessons", nullable = false)
    private Long totalLessons;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;

    @CreationTimestamp
    @Column(name = "completed_at", nullable = false, updatable = false)
    private Instant completedAt;

    public Certificate(String code, String studentName, String courseTitle, Long totalLessons, User user, Course course) {
        this.code = code;
        this.studentName = studentName;
        this.courseTitle = courseTitle;
        this.totalLessons = totalLessons;
        this.user = user;
        this.course = course;
    }
}
