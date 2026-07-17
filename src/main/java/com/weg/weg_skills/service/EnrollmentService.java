package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.EnrollmentRequestDTO;
import com.weg.weg_skills.mapper.EnrollmentMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Enrollment;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.EnrollmentRepository;
import com.weg.weg_skills.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public void enrollUser(EnrollmentRequestDTO dto) {
        if (enrollmentRepository.existsByUserIdAndCourseId(dto.userId(), dto.courseId())) {
            throw new RuntimeException("Usuário já matriculado neste curso.");
        }

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Course course = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new RuntimeException("Curso não encontrado"));

        Enrollment enrollment = enrollmentMapper.toEntity(dto, user, course);
        enrollmentRepository.save(enrollment);
    }
}