package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.ModuleCreateRequestDTO;
import com.weg.weg_skills.dto.ModuleResponseDTO;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Module;

import org.springframework.stereotype.Component;

@Component
public class ModuleMapper {
    public Module toEntity(ModuleCreateRequestDTO dto, Course course) { return new Module(dto.title(), dto.description(), course); }

    public ModuleResponseDTO toResponse(Module module) {
        return new ModuleResponseDTO(module.getId(), module.getTitle(), module.getDescription());
    }
}
