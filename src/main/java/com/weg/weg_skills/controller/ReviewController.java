package com.weg.weg_skills.controller;

import com.weg.weg_skills.service.ReviewService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/reviews")
@AllArgsConstructor
public class ReviewController {
    private ReviewService reviewService;
}
