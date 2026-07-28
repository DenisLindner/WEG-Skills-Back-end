package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.CertificateResponseDTO;
import com.weg.weg_skills.model.Certificate;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.User;
import org.springframework.stereotype.Component;

@Component
public class CertificateMapper {
    public Certificate toEntity(String code, String studentName, String courseTitle, Long totalLessons, Course course, User user) {
        return new Certificate(code, studentName, courseTitle, totalLessons, user, course);
    }

    public CertificateResponseDTO toResponse(Certificate certificate) {
        return new CertificateResponseDTO(certificate.getCode(), certificate.getStudentName(), certificate.getCourseTitle(), certificate.getTotalLessons(), certificate.getCompletedAt());
    }
}
