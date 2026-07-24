package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.ReviewCreateRequestDTO;
import com.weg.weg_skills.dto.ReviewResponseDTO;
import com.weg.weg_skills.dto.ReviewUpdateRequestDTO;
import com.weg.weg_skills.exceptions.ForbiddenException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.exceptions.ReviewAlreadyExistsException;
import com.weg.weg_skills.mapper.ReviewMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Review;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.EnrollmentRepository;
import com.weg.weg_skills.repository.ReviewRepository;
import com.weg.weg_skills.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class ReviewService {
    private ReviewRepository reviewRepository;
    private ReviewMapper reviewMapper;
    private CourseRepository courseRepository;
    private UserRepository userRepository;
    private EnrollmentRepository enrollmentRepository;
    private MediaService mediaService;

    @Transactional
    public ReviewResponseDTO create(ReviewCreateRequestDTO dto, Long userId) {
        Course course = courseRepository.findById(dto.courseId()).orElseThrow(() -> new ResourceNotFoundException("Course", dto.courseId()));

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!enrollmentRepository.existsByUserAndCourse(user, course)) {
            throw new ForbiddenException();
        }

        if (reviewRepository.existsByCourseAndUser(course, user)) {
            throw new ReviewAlreadyExistsException();
        }

        Review review = reviewMapper.toEntity(dto, course, user);

        review = reviewRepository.save(review);

        log.atInfo().addKeyValue("courseId", course.getId()).addKeyValue("userId", userId).log("Review created");

        return reviewMapper.toResponse(review, review.getUser().getImage() != null && review.getUser().getImage().isReady() ? mediaService.getPublicUrl(review.getUser().getImage().getId()) : null);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> findAllByCourse(Long courseId, int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Pagination must have page >= 0, size > 0, and size <= 100");
        }

        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", courseId);
        }

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("createdAt").descending()
        );

        Page<Review> reviews = reviewRepository.findAllByCourseId(courseId, pageable);

        return reviews.map(r -> reviewMapper.toResponse(r, r.getUser().getImage() != null && r.getUser().getImage().isReady() ? mediaService.getPublicUrl(r.getUser().getImage().getId()) : null));
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> findAllByUser(Long userId, int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Pagination must have page >= 0, size > 0, and size <= 100");
        }

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("createdAt").descending()
        );

        Page<Review> reviews = reviewRepository.findAllByUserId(userId, pageable);

        return reviews.map(r -> reviewMapper.toResponse(r, r.getUser().getImage() != null && r.getUser().getImage().isReady() ? mediaService.getPublicUrl(r.getUser().getImage().getId()) : null));
    }

    @Transactional
    public ReviewResponseDTO update(Long id, ReviewUpdateRequestDTO dto, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        Review review = reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review", id));

        if (!Objects.equals(review.getUser().getId(), userId)) {
            throw new ForbiddenException();
        }

        review.setRate(dto.rate());

        review = reviewRepository.save(review);

        log.atInfo().addKeyValue("reviewId", id).addKeyValue("userId", userId).log("Review updated");

        return reviewMapper.toResponse(review, review.getUser().getImage() != null && review.getUser().getImage().isReady() ? mediaService.getPublicUrl(review.getUser().getImage().getId()) : null);
    }

    @Transactional
    public void deleteById(Long id, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Review", id);
        }
        if (!reviewRepository.existsByIdAndUserId(id, userId)) {
            throw new ForbiddenException();
        }

        reviewRepository.deleteById(id);

        log.atInfo().addKeyValue("reviewId", id).addKeyValue("userId", userId).log("Review deleted");
    }
}
