package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.EnrollmentRequestDTO;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Enrollment;
import com.weg.weg_skills.model.User;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    public Enrollment toEntity(EnrollmentRequestDTO dto, User user, Course course) {
        if (dto == null) return null;

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        return enrollment;
    }
}