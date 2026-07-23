package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.DuplicateResourceException;
import com.weg.weg_skills.exceptions.ForbiddenException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.mapper.CourseMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Media;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
    private CourseRepository courseRepository;
    private CourseMapper courseMapper;
    private MediaService mediaService;
    private UserRepository userRepository;

    @Transactional
    public CourseResponseDTO create(CourseCreateRequestDTO dto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user.getRole() != UserRole.INSTRUCTOR && user.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException();
        }

        String title = normalizeString(dto.title());
        if (courseRepository.existsByTitleIgnoreCase(title)) {
            throw new DuplicateResourceException("Course", "title", title);
        }

        String description = dto.description() != null ? normalizeString(dto.description()) : null;
        Course course = courseMapper.toEntity(title, description, user);

        course = courseRepository.save(course);

        return courseMapper.toResponse(course, null);
    }

    @Transactional
    public UploadTicketResponseDTO uploadImage(Long id, CreateMediaUploadRequestDTO dto, Long userId, List<String> roles) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        Course course = courseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course", id));

        if (!course.getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        CreatedMediaUpload createdMedia = mediaService.createCourseImageUpload(course.getId(), userId, dto);

        Media previousImage = course.getPendingImage();

        course.setPendingImage(createdMedia.media());
        courseRepository.saveAndFlush(course);

        if (previousImage != null) {
            mediaService.delete(previousImage.getId());
        }

        return createdMedia.ticket();
    }

    @Transactional(readOnly = true)
    public Page<CourseResponseDTO> findAll(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Pagination must have page >= 0, size > 0, and size <= 100");
        }

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("createdAt").descending()
        );

        Page<Course> courses = courseRepository.findAll(pageable);

        return courses.map(c ->
            courseMapper.toResponse(c, c.getImage() != null && c.getImage().isReady() ? mediaService.getPublicUrl(c.getImage().getId()) : null)
        );
    }

    @Transactional(readOnly = true)
    public CourseResponseDTO findById(Long id) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course", id));

        return courseMapper.toResponse(course, course.getImage() != null && course.getImage().isReady() ? mediaService.getPublicUrl(course.getImage().getId()) : null);
    }

    @Transactional
    public CourseResponseDTO update(Long id, CourseUpdateRequestDTO dto, Long userId, List<String> roles) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course", id));

        if (!course.getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        if (dto.title() != null) {
            String title = normalizeString(dto.title());
            if (!title.equalsIgnoreCase(course.getTitle()) && courseRepository.existsByTitleIgnoreCase(title)) {
                throw new DuplicateResourceException("Course", "title", title);
            }
            course.setTitle(title);
        }

        if (dto.description() != null) {
            String description = normalizeString(dto.description());
            course.setDescription(description);
        }

        course = courseRepository.save(course);

        return courseMapper.toResponse(course, course.getImage() != null && course.getImage().isReady() ? mediaService.getPublicUrl(course.getImage().getId()) : null);
    }

    @Transactional
    public void deleteById(Long id, Long userId, List<String> roles) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course", id));

        if (!course.getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        if (course.getImage() != null) {
            mediaService.delete(course.getImage().getId());
        }

        if (course.getPendingImage() != null) {
            mediaService.delete(course.getPendingImage().getId());
        }

        course.getModules().forEach(m -> {
            m.getLessons().forEach(l -> {
                if (l.getVideo() != null && !l.getVideo().isDeleted()) mediaService.delete(l.getVideo().getId());
                if (l.getPendingVideo() != null && !l.getPendingVideo().isDeleted()) mediaService.delete(l.getPendingVideo().getId());
            });
            if (m.getImage() != null && !m.getImage().isDeleted()) mediaService.delete(m.getImage().getId());
            if (m.getPendingImage() != null && !m.getPendingImage().isDeleted()) mediaService.delete(m.getPendingImage().getId());
        });

        courseRepository.delete(course);
    }

    private String normalizeString(String value) {
        return value.trim();
    }
}
