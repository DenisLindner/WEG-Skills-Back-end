package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.EnrollmentResponseDTO;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Enrollment;
import com.weg.weg_skills.model.User;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    public Enrollment toEntity(User user, Course course) {
        return new Enrollment(user, course);
    }

    public EnrollmentResponseDTO toResponseDTO(Enrollment enrollment) {
        return new EnrollmentResponseDTO(
                enrollment.getUser().getId(),
                enrollment.getCourse().getId(),
                enrollment.getEnrolledAt()
        );
    }
}