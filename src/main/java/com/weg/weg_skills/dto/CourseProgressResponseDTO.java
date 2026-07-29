package com.weg.weg_skills.dto;

import java.util.List;

public record CourseProgressResponseDTO(
        Long courseId,
        Long completedLessons,
        Long totalLessons,
        Double percentage,
        List<LessonProgressResponseDTO> lessons
) {
}