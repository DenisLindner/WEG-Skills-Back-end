package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.EnrollmentRequestDTO;
import com.weg.weg_skills.dto.EnrollmentResponseDTO;
import com.weg.weg_skills.exceptions.EnrollmentAlreadyExistsException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.mapper.EnrollmentMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Enrollment;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.EnrollmentRepository;
import com.weg.weg_skills.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public EnrollmentResponseDTO enrollUser(EnrollmentRequestDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Course course = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", dto.courseId()));

        if (enrollmentRepository.existsByUserAndCourse(user, course)) {
            throw new EnrollmentAlreadyExistsException();
        }

        Enrollment enrollment = enrollmentMapper.toEntity(user, course);
        enrollment = enrollmentRepository.save(enrollment);

        return enrollmentMapper.toResponseDTO(enrollment);
    }
}