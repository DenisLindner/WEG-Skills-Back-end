package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.LessonCreateRequestDTO;
import com.weg.weg_skills.dto.LessonResponseDTO;
import com.weg.weg_skills.dto.LessonUpdateRequestDTO;
import com.weg.weg_skills.mapper.LessonMapper;
import com.weg.weg_skills.model.Lesson;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.repository.LessonRepository;
import com.weg.weg_skills.repository.ModuleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class LessonService {
    private LessonRepository lessonRepository;
    private LessonMapper lessonMapper;
    private ModuleRepository moduleRepository;

    public LessonResponseDTO create(LessonCreateRequestDTO dto) {
        Module module = moduleRepository.findById(dto.moduleId())
                .orElseThrow(() -> new RuntimeException("Module not found"));

        if (lessonRepository.existsByModuleAndTitleIgnoreCase(module, dto.title())) {
            throw new RuntimeException("A lesson with this title already exists in this module");
        }

        Lesson lesson = lessonMapper.toEntity(dto, module);

        lesson = lessonRepository.save(lesson);

        return lessonMapper.toResponse(lesson);
    }

    public List<LessonResponseDTO> findAll() {
        List<Lesson> lessons = lessonRepository.findAll();

        return lessons.stream().map(lessonMapper::toResponse).toList();
    }

    public LessonResponseDTO findById(Long id) {
        Lesson lesson = lessonRepository.findById(id).orElseThrow(RuntimeException::new);

        return lessonMapper.toResponse(lesson);
    }

    public LessonResponseDTO update(Long id, LessonUpdateRequestDTO dto) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        if (dto.title() != null) {
            if (!dto.title().equals(lesson.getTitle()) &&
                    lessonRepository.existsByModuleAndTitleIgnoreCase(lesson.getModule(), dto.title())) {
                throw new RuntimeException("A lesson with this title already exists in this module");
            }
            lesson.setTitle(dto.title());
        }

        if (dto.description() != null) {
            lesson.setDescription(dto.description());
        }

        lesson = lessonRepository.save(lesson);

        return lessonMapper.toResponse(lesson);
    }

    public void deleteById (Long id) {
        if (!lessonRepository.existsById(id)) {
            throw new RuntimeException();
        }

        lessonRepository.deleteById(id);
    }
}