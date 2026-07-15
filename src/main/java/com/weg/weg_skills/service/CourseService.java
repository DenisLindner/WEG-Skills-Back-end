package com.weg.weg_skills.service;

import com.weg.weg_skills.mapper.CourseMapper;
import com.weg.weg_skills.repository.CourseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseService {
    private CourseRepository courseRepository;
    private CourseMapper courseMapper;
}
