package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.mapper.ModuleMapper;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.repository.CourseRepository;
import com.weg.weg_skills.repository.ModuleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ModuleService {
    private ModuleRepository moduleRepository;
    private ModuleMapper moduleMapper;
    private CourseRepository courseRepository;

    public ModuleResponseDTO create(ModuleCreateRequestDTO dto) {
        Course course = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (moduleRepository.existsByCourseAndTitleIgnoreCase(course, dto.title())) {
            throw new RuntimeException("A module with this title already exists in this course");
        }

        Module module = moduleMapper.toEntity(dto, course);

        module = moduleRepository.save(module);

        return moduleMapper.toResponse(module);
    }

    public List<ModuleResponseDTO> findAll() {
        List<Module> modules = moduleRepository.findAll();

        return modules.stream().map(moduleMapper::toResponse).toList();
    }

    public ModuleResponseDTO findById(Long id) {
        Module module = moduleRepository.findById(id).orElseThrow(RuntimeException::new);

        return moduleMapper.toResponse(module);
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

        return moduleMapper.toResponse(module);
    }

    public void deleteById (Long id) {
        if (!moduleRepository.existsById(id)) {
            throw new RuntimeException();
        }

        moduleRepository.deleteById(id);
    }
}