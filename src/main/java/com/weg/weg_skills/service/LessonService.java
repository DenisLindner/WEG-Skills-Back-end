package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.DuplicateResourceException;
import com.weg.weg_skills.exceptions.ForbiddenException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.mapper.LessonMapper;
import com.weg.weg_skills.model.Lesson;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.repository.EnrollmentRepository;
import com.weg.weg_skills.repository.LessonRepository;
import com.weg.weg_skills.repository.ModuleRepository;
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
public class LessonService {
    private LessonRepository lessonRepository;
    private LessonMapper lessonMapper;
    private ModuleRepository moduleRepository;
    private MediaService mediaService;
    private UserRepository userRepository;
    private EnrollmentRepository enrollmentRepository;

    @Transactional
    public LessonResponseDTO create(LessonCreateRequestDTO dto, Long userId, List<String> roles) {
        Module module = moduleRepository.findById(dto.moduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Module", dto.moduleId()));

        if (!module.getCourse().getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        String title = normalizeString(dto.title());
        if (lessonRepository.existsByModuleAndTitleIgnoreCase(module, title)) {
            throw new DuplicateResourceException("Lesson", "title", title);
        }

        String description = dto.description() != null ? normalizeString(dto.description()) : null;
        Lesson lesson = lessonMapper.toEntity(title, description, module);

        lesson = lessonRepository.save(lesson);

        return lessonMapper.toResponse(lesson);
    }

    @Transactional
    public UploadTicketResponseDTO uploadVideo(Long id, CreateMediaUploadRequestDTO dto, Long userId, List<String> roles) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        Lesson lesson = lessonRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        if (!lesson.getModule().getCourse().getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        CreatedMediaUpload createdMedia = mediaService.createLessonVideoUpload(lesson.getModule().getCourse().getId(), lesson.getModule().getId(), lesson.getId(), userId, dto);

        if (lesson.getVideo() != null) {
            mediaService.delete(lesson.getVideo().getId());
        }
        lesson.setVideo(createdMedia.media());
        lessonRepository.save(lesson);

        return createdMedia.ticket();
    }

    @Transactional(readOnly = true)
    public Page<LessonResponseDTO> findAllByModule(Long moduleId, int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Pagination must have page >= 0, size > 0, and size <= 100");
        }

        if (!moduleRepository.existsById(moduleId)) {
            throw new ResourceNotFoundException("Module", moduleId);
        }

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("createdAt").descending()
        );

        Page<Lesson> lessons = lessonRepository.findAllByModuleId(moduleId, pageable);

        return lessons.map(lessonMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public LessonDetailsResponseDTO findById(Long id, Long userId, List<String> roles) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        Lesson lesson = lessonRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        if (!enrollmentRepository.existsByUserIdAndCourse(userId, lesson.getModule().getCourse())) {
            if (!lesson.getModule().getCourse().getInstructor().getId().equals(userId)) {
                if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                    throw new ForbiddenException();
                }
            }
        }

        return lessonMapper.toResponseDetails(lesson, lesson.getVideo() != null && lesson.getVideo().isReady() ? mediaService.getPlaybackVideoUrl(lesson.getVideo().getId()): null);
    }

    @Transactional
    public LessonResponseDTO update(Long id, LessonUpdateRequestDTO dto, Long userId, List<String> roles) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        if (!lesson.getModule().getCourse().getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        if (dto.title() != null) {
            String title = normalizeString(dto.title());
            if (!title.equalsIgnoreCase(lesson.getTitle()) && lessonRepository.existsByModuleAndTitleIgnoreCase(lesson.getModule(), title)) {
                throw new DuplicateResourceException("Lesson", "title", title);
            }
            lesson.setTitle(title);
        }

        if (dto.description() != null) {
            String description = normalizeString(dto.description());
            lesson.setDescription(description);
        }

        lesson = lessonRepository.save(lesson);

        return lessonMapper.toResponse(lesson);
    }

    @Transactional
    public void deleteById (Long id, Long userId, List<String> roles) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

        if (!lesson.getModule().getCourse().getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        if (lesson.getVideo() != null) {
            mediaService.delete(lesson.getVideo().getId());
        }

        lessonRepository.delete(lesson);
    }

    private String normalizeString(String value) {
        return value.trim();
    }
}