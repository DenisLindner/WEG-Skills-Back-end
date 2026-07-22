package com.weg.weg_skills.repository;

import com.weg.weg_skills.model.Lesson;
import com.weg.weg_skills.model.Module;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Boolean existsByModuleAndTitleIgnoreCase(Module module, String title);
    Page<Lesson> findAllByModuleId(Long moduleId, Pageable pageable);
}
