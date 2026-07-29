package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.enums.CourseStatus;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.DuplicateResourceException;
import com.weg.weg_skills.exceptions.ForbiddenException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.mapper.ModuleMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Media;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.ModuleRepository;
import com.weg.weg_skills.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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
public class ModuleService {
    private ModuleRepository moduleRepository;
    private ModuleMapper moduleMapper;
    private CourseRepository courseRepository;
    private MediaService mediaService;
    private UserRepository userRepository;

    @Transactional
    @CacheEvict(cacheNames = "topCourses", allEntries = true)
    public ModuleResponseDTO create(ModuleCreateRequestDTO dto, Long userId, List<String> roles) {
        Course course = courseRepository.findById(dto.courseId()).orElseThrow(() -> new ResourceNotFoundException("Course", dto.courseId()));

        if (!course.getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        String title = normalizeString(dto.title());
        if (moduleRepository.existsByCourseAndTitleIgnoreCase(course, title)) {
            throw new DuplicateResourceException("Module", "title", title);
        }

        Module lastModule = moduleRepository.findTopByCourseOrderByPositionDesc(course);

        long position = lastModule != null ? lastModule.getPosition() + 1 : 1;

        if (position > 100) {
            if (moduleRepository.countByCourseId(course.getId()) >= 100) {
                throw new IllegalArgumentException("Module limit per courses reached");
            }
        }

        String description = dto.description() != null ? normalizeString(dto.description()) : null;
        Module module = moduleMapper.toEntity(title, description, course, position);

        module = moduleRepository.save(module);
        course.markAsDraft();

        log.atInfo().addKeyValue("title", title).addKeyValue("courseId", module.getCourse().getId()).addKeyValue("userId", userId).log("Module created");

        return moduleMapper.toResponse(module, null);
    }

    @Transactional
    public UploadTicketResponseDTO uploadImage(Long id, CreateMediaUploadRequestDTO dto, Long userId, List<String> roles) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        Module module = moduleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Module", id));

        if (!module.getCourse().getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        CreatedMediaUpload createdMedia = mediaService.createModuleImageUpload(module.getCourse().getId(), module.getId(), userId, dto);

        Media previousImage = module.getPendingImage();

        module.setPendingImage(createdMedia.media());
        moduleRepository.saveAndFlush(module);

        if (previousImage != null) {
            mediaService.delete(previousImage.getId());
        }

        log.atInfo().addKeyValue("moduleId", id).addKeyValue("userId", userId).log("Upload module image ticket created");

        return createdMedia.ticket();
    }

    @Transactional(readOnly = true)
    public Page<ModuleResponseDTO> findAllByCourse(Long courseId, int page, int size, Long userId, List<String> roles) {
        validatePagination(page, size);

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        if (course.getCourseStatus() != CourseStatus.PUBLISHED) {
            if (!course.getInstructor().getId().equals(userId)) {
                if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                    throw new ForbiddenException();
                }
            }
        }

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("position").ascending()
        );

        Page<Module> modules = moduleRepository.findAllByCourseId(courseId, pageable);

        return modules.map(m -> moduleMapper.toResponse(m, m.getImage() != null && m.getImage().isReady() ? mediaService.getPublicUrl(m.getImage()) : null));
    }

    @Transactional(readOnly = true)
    public ModuleResponseDTO findById(Long id, Long userId, List<String> roles) {
        Module module = moduleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Module", id));

        if (module.getCourse().getCourseStatus() != CourseStatus.PUBLISHED) {
            if (!module.getCourse().getInstructor().getId().equals(userId)) {
                if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                    throw new ForbiddenException();
                }
            }
        }

        return moduleMapper.toResponse(module, module.getImage() != null && module.getImage().isReady() ? mediaService.getPublicUrl(module.getImage()) : null);
    }

    @Transactional
    public ModuleResponseDTO update(Long id, ModuleUpdateRequestDTO dto, Long userId, List<String> roles) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module", id));

        if (!module.getCourse().getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        if (dto.title() != null) {
            String title = normalizeString(dto.title());
            validateTitle(title);
            if (!title.equalsIgnoreCase(module.getTitle()) && moduleRepository.existsByCourseAndTitleIgnoreCase(module.getCourse(), title)) {
                throw new DuplicateResourceException("Module", "title", dto.title());
            }
            module.setTitle(title);
        }

        if (dto.description() != null) {
            String description = normalizeString(dto.description());
            module.setDescription(description);
        }

        module = moduleRepository.save(module);

        log.atInfo().addKeyValue("moduleId", id).addKeyValue("userId", userId).log("Module updated");

        return moduleMapper.toResponse(module, module.getImage() != null && module.getImage().isReady() ? mediaService.getPublicUrl(module.getImage()) : null);
    }

    @Transactional
    @CacheEvict(cacheNames = "topCourses", allEntries = true)
    public void deleteById (Long id, Long userId, List<String> roles) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module", id));

        if (!module.getCourse().getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        if (module.getImage() != null) {
            mediaService.delete(module.getImage().getId());
        }

        if (module.getPendingImage() != null) {
            mediaService.delete(module.getPendingImage().getId());
        }

        module.getLessons().forEach(l -> {
            if (l.getVideo() != null && !l.getVideo().isDeleted()) mediaService.delete(l.getVideo().getId());
            if (l.getPendingVideo() != null && !l.getPendingVideo().isDeleted()) mediaService.delete(l.getPendingVideo().getId());
        });

        module.getCourse().markAsDraft();
        moduleRepository.delete(module);

        log.atInfo().addKeyValue("moduleId", id).addKeyValue("userId", userId).log("Module deleted");
    }

    @Transactional
    public void reposition(RepositionRequestDTO dto, Long userId, List<String> roles) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        if (!courseRepository.existsById(dto.parentId())) {
            throw new ResourceNotFoundException("Course", dto.parentId());
        }

        List<Module> modules = moduleRepository.findAllByCourseId(dto.parentId());

        if (dto.orderedIds().isEmpty()) {
            throw new IllegalArgumentException("Ordered ids list is empty");
        }

        if (modules.isEmpty()) {
            throw new IllegalArgumentException("Modules list is empty");
        }

        for (Module module : modules) {
            if (!dto.orderedIds().contains(module.getId())) {
                throw new IllegalArgumentException("The size of the module list is different from the size of the ordered ID list");
            }
        }

        if (modules.size() != dto.orderedIds().size()) {
            throw new IllegalArgumentException("The size of the module list is different from the size of the ordered ID list");
        }

        if (!Objects.equals(modules.getFirst().getCourse().getInstructor().getId(), userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))) {
                throw new ForbiddenException();
            }
        }

        Long position = 1L;
        for (Long id : dto.orderedIds()) {
            Module module = modules.stream().filter(m -> m.getId().equals(id)).findFirst().orElseThrow(() -> new ResourceNotFoundException("Module", id));

            module.setPosition(position);
            position ++;
        }

        moduleRepository.saveAllAndFlush(modules);
    }

    private String normalizeString(String value) {
        return value.trim();
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().length() < 3) {
            throw new IllegalArgumentException("Title must have a non-null value and be equal to or longer than 3 characters");
        }
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Pagination must have page >= 0, size > 0, and size <= 100");
        }
    }
}
