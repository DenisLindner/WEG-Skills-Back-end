package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.DuplicateResourceException;
import com.weg.weg_skills.exceptions.ForbiddenException;
import com.weg.weg_skills.exceptions.ResourceNotFoundException;
import com.weg.weg_skills.mapper.ModuleMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.ModuleRepository;
import com.weg.weg_skills.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class ModuleService {
    private ModuleRepository moduleRepository;
    private ModuleMapper moduleMapper;
    private CourseRepository courseRepository;
    private MediaService mediaService;
    private UserRepository userRepository;

    @Transactional
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

        String description = dto.description() != null ? normalizeString(dto.description()) : null;
        Module module = moduleMapper.toEntity(title, description, course);

        module = moduleRepository.save(module);

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

        if (module.getImage() != null) {
            mediaService.delete(module.getImage().getId());
        }
        module.setImage(createdMedia.media());
        moduleRepository.save(module);

        return createdMedia.ticket();
    }

    @Transactional(readOnly = true)
    public Page<ModuleResponseDTO> findAllByCourse(Long courseId, int page, int size) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", courseId);
        }

        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException();
        }

        Pageable pageable = PageRequest.of(
                page, size
        );

        Page<Module> modules = moduleRepository.findAllByCourseIdOrderByCreatedAtDesc(courseId, pageable);

        return modules.map(m -> moduleMapper.toResponse(m, m.getImage() != null && m.getImage().isReady() ? mediaService.getPublicUrl(m.getImage().getId()) : null));
    }

    @Transactional(readOnly = true)
    public ModuleResponseDTO findById(Long id) {
        Module module = moduleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Module", id));

        return moduleMapper.toResponse(module, module.getImage() != null && module.getImage().isReady() ? mediaService.getPublicUrl(module.getImage().getId()) : null);
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

        return moduleMapper.toResponse(module, module.getImage() != null && module.getImage().isReady() ? mediaService.getPublicUrl(module.getImage().getId()) : null);
    }

    @Transactional
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

        moduleRepository.delete(module);
    }

    private String normalizeString(String value) {
        return value.trim();
    }
}