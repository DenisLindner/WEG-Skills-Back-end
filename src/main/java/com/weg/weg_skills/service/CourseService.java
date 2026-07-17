package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.mapper.CourseMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.repository.CourseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
    private CourseRepository courseRepository;
    private CourseMapper courseMapper;
    private MediaService mediaService;

    public CourseResponseDTO create(CourseCreateRequestDTO dto) {
        if (courseRepository.existsByTitleIgnoreCase(dto.title())) {
            throw new RuntimeException();
        }

        Course course = courseMapper.toEntity(dto);

        course = courseRepository.save(course);

        return courseMapper.toResponse(course, null);
    }

    @Transactional
    public UploadTicketResponseDTO uploadImage(Long id, CreateMediaUploadRequestDTO dto) {
        Course course = courseRepository.findById(id).orElseThrow(RuntimeException::new);

        CreatedMediaUpload createdMedia = mediaService.createCourseImageUpload(course.getId(), null, dto);

        if (course.getImage() != null) {
            mediaService.delete(course.getImage().getId());
        }
        course.setImage(createdMedia.media());
        courseRepository.save(course);

        return createdMedia.ticket();
    }

    public List<CourseResponseDTO> findAll() {
        List<Course> courses = courseRepository.findAll();

        return courses.stream().map(c ->
            courseMapper.toResponse(c, c.getImage() != null && c.getImage().isReady() ? mediaService.getPublicUrl(c.getImage().getId()) : null)
        ).toList();
    }

    public CourseResponseDTO findById(Long id) {
        Course course = courseRepository.findById(id).orElseThrow(RuntimeException::new);

        return courseMapper.toResponse(course, course.getImage() != null && course.getImage().isReady() ? mediaService.getPublicUrl(course.getImage().getId()) : null);
    }

    public CourseResponseDTO update(Long id, CourseUpdateRequestDTO dto) {
        Course course = courseRepository.findById(id).orElseThrow(RuntimeException::new);

        if (dto.title() != null) {
            if (!dto.title().equals(course.getTitle()) && courseRepository.existsByTitleIgnoreCase(dto.title())) {
                throw new RuntimeException();
            }
            course.setTitle(dto.title());
        }

        if (dto.description() != null) {
            course.setDescription(dto.description());
        }

        course = courseRepository.save(course);

        return courseMapper.toResponse(course, course.getImage() != null && course.getImage().isReady() ? mediaService.getPublicUrl(course.getImage().getId()) : null);
    }

    public void deleteById(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException();
        }

        courseRepository.deleteById(id);
    }
}
