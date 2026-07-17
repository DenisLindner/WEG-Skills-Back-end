package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.mapper.ModuleMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.ModuleRepository;
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

    public ModuleResponseDTO create(ModuleCreateRequestDTO dto) {
        Course course = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (moduleRepository.existsByCourseAndTitleIgnoreCase(course, dto.title())) {
            throw new RuntimeException("A module with this title already exists in this course");
        }

        Module module = moduleMapper.toEntity(dto, course);

        module = moduleRepository.save(module);

        return moduleMapper.toResponse(module, null);
    }

    @Transactional
    public UploadTicketResponseDTO uploadImage(Long id, CreateMediaUploadRequestDTO dto) {
        Module module = moduleRepository.findById(id).orElseThrow(RuntimeException::new);

        CreatedMediaUpload createdMedia = mediaService.createModuleImageUpload(module.getCourse().getId(), module.getId(), null, dto);

        if (module.getImage() != null) {
            mediaService.delete(module.getImage().getId());
        }
        module.setImage(createdMedia.media());
        moduleRepository.save(module);

        return createdMedia.ticket();
    }

    public List<ModuleResponseDTO> findAllByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new RuntimeException();
        }

        List<Module> modules = moduleRepository.findAllByCourseId(courseId);

        return modules.stream().map(m -> moduleMapper.toResponse(m, m.getImage() != null && m.getImage().isReady() ? mediaService.getPublicUrl(m.getImage().getId()) : null)).toList();
    }

    public ModuleResponseDTO findById(Long id) {
        Module module = moduleRepository.findById(id).orElseThrow(RuntimeException::new);

        return moduleMapper.toResponse(module, module.getImage() != null && module.getImage().isReady() ? mediaService.getPublicUrl(module.getImage().getId()) : null);
    }

    public ModuleResponseDTO update(Long id, ModuleUpdateRequestDTO dto) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        if (dto.title() != null) {
            if (!dto.title().equals(module.getTitle()) &&
                    moduleRepository.existsByCourseAndTitleIgnoreCase(module.getCourse(), dto.title())) {
                throw new RuntimeException("A module with this title already exists in this course");
            }
            module.setTitle(dto.title());
        }

        if (dto.description() != null) {
            module.setDescription(dto.description());
        }

        module = moduleRepository.save(module);

        return moduleMapper.toResponse(module, module.getImage() != null && module.getImage().isReady() ? mediaService.getPublicUrl(module.getImage().getId()) : null);
    }

    public void deleteById (Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new RuntimeException();
        }

        moduleRepository.deleteById(id);
    }
}