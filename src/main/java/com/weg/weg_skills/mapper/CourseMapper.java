package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.CourseResponseDTO;
import com.weg.weg_skills.dto.CourseWithRatingResponseDTO;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.projection.CourseWithRatingProjection;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {
    public Course toEntity(String title, String description, User user) {
        return new Course(title, description, user);
    }

    public CourseResponseDTO toResponse(Course course, String url) {
        return new CourseResponseDTO(course.getId(), course.getTitle(), course.getDescription(), url);
    }

    public CourseWithRatingResponseDTO toResponseProjection(CourseWithRatingProjection projection, String url) {
        return new CourseWithRatingResponseDTO(projection.getId(), projection.getTitle(), projection.getDescription(), projection.getRating(), url);
    }
}
