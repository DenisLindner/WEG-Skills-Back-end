package com.weg.weg_skills.projection;

import com.weg.weg_skills.model.Media;

public interface CourseWithRatingProjection {
    Long getId();
    String getTitle();
    String getDescription();
    Double getRating();
    Media getImage();
}
