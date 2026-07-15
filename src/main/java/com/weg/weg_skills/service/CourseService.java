package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.CourseCreateRequestDTO;
import com.weg.weg_skills.dto.CourseResponseDTO;
import com.weg.weg_skills.mapper.CourseMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.repository.CourseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseService {
    private CourseRepository courseRepository;
    private CourseMapper courseMapper;

    public CourseResponseDTO create(CourseCreateRequestDTO dto) {
        if (courseRepository.existsByTitleIgnoreCase(dto.title())) {
            throw new RuntimeException();
        }

        Course course = courseMapper.toEntity(dto);

        course = courseRepository.save(course);

        return courseMapper.toResponse(course);
    }
}
