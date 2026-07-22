package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.ModuleResponseDTO;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Module;

import org.springframework.stereotype.Component;

@Component
public class ModuleMapper {
    public Module toEntity(String title, String description, Course course) { return new Module(title, description, course); }

    public ModuleResponseDTO toResponse(Module module, String url) {
        return new ModuleResponseDTO(module.getId(), module.getTitle(), module.getDescription(), url);
    }
}
