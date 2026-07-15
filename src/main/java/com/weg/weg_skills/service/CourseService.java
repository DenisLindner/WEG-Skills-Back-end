package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.CourseCreateRequestDTO;
import com.weg.weg_skills.dto.CourseResponseDTO;
import com.weg.weg_skills.mapper.CourseMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.repository.CourseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<CourseResponseDTO> findAll() {
        List<Course> courses = courseRepository.findAll();

        return courses.stream().map(courseMapper::toResponse).toList();
    }

    public  CourseResponseDTO findById(Long id) {
        Course course = courseRepository.findById(id).orElseThrow(RuntimeException::new);

        return courseMapper.toResponse(course);
    }
}
