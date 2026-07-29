package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.enums.CourseStatus;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.DuplicateResourceException;
import com.weg.weg_skills.exceptions.EnrollmentNotFoundException;
import com.weg.weg_skills.exceptions.ForbiddenException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.mapper.LessonMapper;
import com.weg.weg_skills.mapper.LessonProgressMapper;
import com.weg.weg_skills.model.*;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class LessonService {
    private LessonRepository lessonRepository;
    private LessonMapper lessonMapper;
    private ModuleRepository moduleRepository;
    private MediaService mediaService;
    private UserRepository userRepository;
    private EnrollmentRepository enrollmentRepository;
    private LessonProgressRepository lessonProgressRepository;
    private LessonProgressMapper lessonProgressMapper;

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

        Lesson lastLesson = lessonRepository.findTopByModuleOrderByPositionDesc(module);

        long position = lastLesson != null ? lastLesson.getPosition() + 1 : 1;

        if (position > 100) {
            if (lessonRepository.countByModuleId(module.getId()) >= 100) {
                throw new IllegalArgumentException("Class limit per module reached");
            }
        }

        String description = dto.description() != null ? normalizeString(dto.description()) : null;
        Lesson lesson = lessonMapper.toEntity(title, description, module, position);

        lesson = lessonRepository.save(lesson);

        log.atInfo().addKeyValue("title", title).addKeyValue("moduleId", lesson.getModule().getId())
                .addKeyValue("courseId", lesson.getModule().getCourse().getId()).addKeyValue("userId", userId).log("Lesson created");

        return lessonMapper.toResponse(lesson);
    }

    @Transactional
    public LessonProgressDetailsResponseDTO completeLesson(Long lessonId, Long userId, List<String> roles) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        if (lesson.getModule().getCourse().getCourseStatus() != CourseStatus.PUBLISHED) {
            if (!lesson.getModule().getCourse().getInstructor().getId().equals(userId)) {
                if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                    throw new ForbiddenException();
                }
            }
        }

        Enrollment enrollment = enrollmentRepository.findByCourseIdAndUserId(lesson.getModule().getCourse().getId(), userId).orElseThrow(() -> new EnrollmentNotFoundException(lesson.getModule().getCourse().getId(), userId));

        LessonProgress lessonProgress = lessonProgressRepository.findByEnrollmentIdAndLessonId(enrollment.getId(), lesson.getId());

        if (lessonProgress != null) {
            return lessonProgressMapper.toResponseDetails(lessonProgress);
        }

        lessonProgress = lessonProgressMapper.toEntity(enrollment, lesson);

        lessonProgress = lessonProgressRepository.save(lessonProgress);

        return lessonProgressMapper.toResponseDetails(lessonProgress);
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

        Media previousVideo = lesson.getPendingVideo();

        lesson.setPendingVideo(createdMedia.media());
        lessonRepository.saveAndFlush(lesson);

        if (previousVideo != null) {
            mediaService.delete(previousVideo.getId());
        }

        log.atInfo().addKeyValue("lessonId", id).addKeyValue("userId", userId).log("Upload lesson video ticket created");

        return createdMedia.ticket();
    }

    @Transactional(readOnly = true)
    public Page<LessonResponseDTO> findAllByModule(Long moduleId, int page, int size, Long userId, List<String> roles) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Pagination must have page >= 0, size > 0, and size <= 100");
        }

        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> new ResourceNotFoundException("Module", moduleId));

        if (module.getCourse().getCourseStatus() != CourseStatus.PUBLISHED) {
            if (!module.getCourse().getInstructor().getId().equals(userId)) {
                if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                    throw new ForbiddenException();
                }
            }
        }

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("position").ascending()
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

        if (lesson.getModule().getCourse().getCourseStatus() != CourseStatus.PUBLISHED) {
            if (!lesson.getModule().getCourse().getInstructor().getId().equals(userId)) {
                if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                    throw new ForbiddenException();
                }
            }
        }

        if (!enrollmentRepository.existsByUserIdAndCourse(userId, lesson.getModule().getCourse())) {
            if (!lesson.getModule().getCourse().getInstructor().getId().equals(userId)) {
                if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                    throw new ForbiddenException();
                }
            }
        }

        return lessonMapper.toResponseDetails(lesson, lesson.getVideo() != null && lesson.getVideo().isReady() ? mediaService.getPlaybackVideoUrl(lesson.getVideo()): null);
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

        log.atInfo().addKeyValue("lesson", id).addKeyValue("userId", userId).log("Lesson updated");

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

        if (lesson.getPendingVideo() != null) {
            mediaService.delete(lesson.getPendingVideo().getId());
        }

        lessonRepository.delete(lesson);

        log.atInfo().addKeyValue("lessonId", id).addKeyValue("userId", userId).log("Lesson deleted");
    }

    @Transactional
    public void reposition(RepositionRequestDTO dto, Long userId, List<String> roles) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        if (!moduleRepository.existsById(dto.parentId())) {
            throw new ResourceNotFoundException("Module", dto.parentId());
        }

        List<Lesson> lessons = lessonRepository.findAllByModuleId(dto.parentId());

        if (dto.orderedIds().isEmpty()) {
            throw new IllegalArgumentException("Ordered ids list is empty");
        }

        if (lessons.isEmpty()) {
            throw new IllegalArgumentException("Lesson list is empty");
        }

        for (Lesson lesson : lessons) {
            if (!dto.orderedIds().contains(lesson.getId())) {
                throw new IllegalArgumentException("The size of the lesson list is different from the size of the ordered ID list");
            }
        }

        if (lessons.size() != dto.orderedIds().size()) {
            throw new IllegalArgumentException("The size of the lesson list is different from the size of the ordered ID list");
        }

        if (!Objects.equals(lessons.getFirst().getModule().getCourse().getInstructor().getId(), userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))) {
                throw new ForbiddenException();
            }
        }

        Long position = 1L;
        for (Long id : dto.orderedIds()) {
            Lesson lesson = lessons.stream().filter(i -> i.getId().equals(id)).findFirst().orElseThrow(() -> new ResourceNotFoundException("Lesson", id));

            lesson.setPosition(position);
            position ++;
        }

        lessonRepository.saveAllAndFlush(lessons);
    }

    private String normalizeString(String value) {
        return value.trim();
    }
}