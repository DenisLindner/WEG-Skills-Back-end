package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.ReviewCreateRequestDTO;
import com.weg.weg_skills.dto.ReviewResponseDTO;
import com.weg.weg_skills.dto.ReviewUpdateRequestDTO;
import com.weg.weg_skills.mapper.ReviewMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Review;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.ReviewRepository;
import com.weg.weg_skills.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ReviewService {
    private ReviewRepository reviewRepository;
    private ReviewMapper reviewMapper;
    private CourseRepository courseRepository;
    private UserRepository userRepository;

    public ReviewResponseDTO create(ReviewCreateRequestDTO dto) {
        Course course = courseRepository.findById(dto.courseId()).orElseThrow(RuntimeException::new);

        User user = userRepository.findById(dto.userId()).orElseThrow(RuntimeException::new);

        if (reviewRepository.existsByCourseAndUser(course, user)) {
            throw new RuntimeException();
        }

        Review review = reviewMapper.toEntity(dto, course, user);

        review = reviewRepository.save(review);

        return reviewMapper.toResponse(review);
    }

    public List<ReviewResponseDTO> findAllByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException();
        }

        List<Review> reviews = reviewRepository.findAllByCourseId(courseId);

        return reviews.stream().map(reviewMapper::toResponse).toList();
    }

    public ReviewResponseDTO update(Long id, ReviewUpdateRequestDTO dto) {
        Review review = reviewRepository.findById(id).orElseThrow(RuntimeException::new);

        review.setRate(dto.rate());

        review = reviewRepository.save(review);

        return reviewMapper.toResponse(review);
    }

    public void deleteById(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException();
        }

        reviewRepository.deleteById(id);
    }
}
