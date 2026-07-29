package com.weg.weg_skills.projection;

import com.weg.weg_skills.enums.CourseStatus;
import com.weg.weg_skills.model.Media;

public interface CourseWithRatingProjection {
    Long getId();
    String getTitle();
    String getDescription();
    CourseStatus getCourseStatus();
    Double getRating();
    Media getImage();
}
