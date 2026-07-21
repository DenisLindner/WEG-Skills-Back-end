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

    public ModuleResponseDTO create(ModuleCreateRequestDTO dto, Long userId, List<String> roles) {
        Course course = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", dto.courseId()));

        if (!course.getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        if (moduleRepository.existsByCourseAndTitleIgnoreCase(course, dto.title())) {
            throw new DuplicateResourceException("Module", "title", dto.title());
        }

        Module module = moduleMapper.toEntity(dto, course);

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

    public List<ModuleResponseDTO> findAllByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", courseId);
        }

        List<Module> modules = moduleRepository.findAllByCourseId(courseId);

        return modules.stream().map(m -> moduleMapper.toResponse(m, m.getImage() != null && m.getImage().isReady() ? mediaService.getPublicUrl(m.getImage().getId()) : null)).toList();
    }

    public ModuleResponseDTO findById(Long id) {
        Module module = moduleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Module", id));

        return moduleMapper.toResponse(module, module.getImage() != null && module.getImage().isReady() ? mediaService.getPublicUrl(module.getImage().getId()) : null);
    }

    public ModuleResponseDTO update(Long id, ModuleUpdateRequestDTO dto, Long userId, List<String> roles) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module", id));

        if (!module.getCourse().getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        if (dto.title() != null) {
            if (!dto.title().equalsIgnoreCase(module.getTitle()) &&
                    moduleRepository.existsByCourseAndTitleIgnoreCase(module.getCourse(), dto.title())) {
                throw new DuplicateResourceException("Module", "title", dto.title());
            }
            module.setTitle(dto.title());
        }

        if (dto.description() != null) {
            module.setDescription(dto.description());
        }

        module = moduleRepository.save(module);

        return moduleMapper.toResponse(module, module.getImage() != null && module.getImage().isReady() ? mediaService.getPublicUrl(module.getImage().getId()) : null);
    }

    public void deleteById (Long id, Long userId, List<String> roles) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Module", id));

        if (!module.getCourse().getInstructor().getId().equals(userId)) {
            if (!roles.contains(String.valueOf(UserRole.ADMIN))){
                throw new ForbiddenException();
            }
        }

        moduleRepository.delete(module);
    }
}