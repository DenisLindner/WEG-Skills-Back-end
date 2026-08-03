package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.EnrollmentRequestDTO;
import com.weg.weg_skills.dto.EnrollmentResponseDTO;
import com.weg.weg_skills.enums.CourseStatus;
import com.weg.weg_skills.exceptions.EnrollmentAlreadyExistsException;
import com.weg.weg_skills.exceptions.EnrollmentNotFoundException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.mapper.EnrollmentMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Enrollment;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.EnrollmentRepository;
import com.weg.weg_skills.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Transactional
    @CacheEvict(cacheNames = "topCourses", allEntries = true)
    public EnrollmentResponseDTO enrollUser(EnrollmentRequestDTO dto, Long userId) {
        Course course = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", dto.courseId()));

        if (course.getCourseStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalStateException("Course is not published");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (enrollmentRepository.existsByUserAndCourse(user, course)) {
            throw new EnrollmentAlreadyExistsException();
        }

        Enrollment enrollment = enrollmentMapper.toEntity(user, course);
        enrollment = enrollmentRepository.save(enrollment);

        log.atInfo().addKeyValue("courseId", course.getId()).addKeyValue("userId", userId).log("Enrollment created");

        return enrollmentMapper.toResponseDTO(enrollment);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDTO> getMeEnrollments(Long id, int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Pagination must have page >= 0, size > 0, and size <= 100");
        }

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("createdAt").descending()
        );

        Page<Enrollment> enrollments = enrollmentRepository.findAllByUser(user, pageable);

        return enrollments.map(enrollmentMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public EnrollmentResponseDTO getMeEnrollmentByCourse(Long courseId, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", courseId);
        }

        Enrollment enrollment = enrollmentRepository.findByCourseIdAndUserId(courseId, userId).orElseThrow(() -> new EnrollmentNotFoundException(courseId, userId));

        return enrollmentMapper.toResponseDTO(enrollment);
    }
}