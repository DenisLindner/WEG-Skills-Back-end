package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.ReviewCreateRequestDTO;
import com.weg.weg_skills.dto.ReviewResponseDTO;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Review;
import com.weg.weg_skills.model.User;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public Review toEntity(ReviewCreateRequestDTO dto, Course course, User user) {
        return new Review(dto.rate(), course, user);
    }

    public ReviewResponseDTO toResponse(Review review) {
        return new ReviewResponseDTO(review.getId(), review.getRate(), review.getCourse().getTitle(), review.getUser().getName(), review.getUser().getPictureUrl());
    }
}
