package com.weg.weg_skills.controller;

import com.weg.weg_skills.service.CourseService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "courses")
@AllArgsConstructor
public class CourseController {
    private CourseService courseService;
}
