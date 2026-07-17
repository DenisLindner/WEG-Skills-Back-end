package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.CourseCreateRequestDTO;
import com.weg.weg_skills.dto.CourseResponseDTO;
import com.weg.weg_skills.model.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {
    public Course toEntity(CourseCreateRequestDTO dto) {
        return new Course(dto.title(), dto.description());
    }

    public CourseResponseDTO toResponse(Course course) {
        return new CourseResponseDTO(course.getId(), course.getTitle(), course.getDescription());
    }
}
