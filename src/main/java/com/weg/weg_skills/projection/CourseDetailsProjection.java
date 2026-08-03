package com.weg.weg_skills.projection;

public interface CourseDetailsProjection extends CourseWithRatingProjection {
    Long getInstructorId();
}
