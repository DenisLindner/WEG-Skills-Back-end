package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.mapper.LessonMapper;
import com.weg.weg_skills.model.Lesson;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.repository.LessonRepository;
import com.weg.weg_skills.repository.ModuleRepository;
import lombok.AllArgsConstructor;
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

    @Transactional
    public UploadTicketResponseDTO uploadVideo(Long id, CreateMediaUploadRequestDTO dto) {
        Lesson lesson = lessonRepository.findById(id).orElseThrow(RuntimeException::new);

        CreatedMediaUpload createdMedia = mediaService.createLessonVideoUpload(lesson.getModule().getCourse().getId(), lesson.getModule().getId(), lesson.getId(), null, dto);

        if (lesson.getVideo() != null) {
            mediaService.delete(lesson.getVideo().getId());
        }
        lesson.setVideo(createdMedia.media());
        lessonRepository.save(lesson);

        return createdMedia.ticket();
    }

    public List<LessonResponseDTO> findAllByModule(Long moduleId) {
        if (!moduleRepository.existsById(moduleId)) {
            throw new RuntimeException();
        }

        List<Lesson> lessons = lessonRepository.findAllByModuleId(moduleId);

        return lessons.stream().map(lessonMapper::toResponse).toList();
    }

    public LessonDetailsResponseDTO findById(Long id) {
        Lesson lesson = lessonRepository.findById(id).orElseThrow(RuntimeException::new);

        return lessonMapper.toResponseDetails(lesson, lesson.getVideo() != null && lesson.getVideo().isReady() ? mediaService.getPlaybackVideoUrl(lesson.getVideo().getId()): null);
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