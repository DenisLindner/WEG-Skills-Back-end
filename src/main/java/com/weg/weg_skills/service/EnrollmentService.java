package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.EnrollmentRequestDTO;
import com.weg.weg_skills.dto.EnrollmentResponseDTO;
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

    public EnrollmentResponseDTO enrollUser(EnrollmentRequestDTO dto) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new RuntimeException("User not found."));
        Course course = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new RuntimeException("Course not found."));

        if (enrollmentRepository.existsByUserAndCourse(user, course)) {
            throw new RuntimeException("User already enrolled in this course.");
        }

        Enrollment enrollment = enrollmentMapper.toEntity(user, course);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return enrollmentMapper.toResponseDTO(savedEnrollment);
    }
}