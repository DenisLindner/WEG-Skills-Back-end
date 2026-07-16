package com.weg.weg_skills.service;

import com.weg.weg_skills.mapper.ReviewMapper;
import com.weg.weg_skills.repository.ReviewRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReviewService {
    private ReviewRepository reviewRepository;
    private ReviewMapper reviewMapper;
}
