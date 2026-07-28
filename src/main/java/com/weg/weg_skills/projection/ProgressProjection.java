package com.weg.weg_skills.projection;

public interface ProgressProjection {
    Long getCourseId();
    Long getCompletedLessons();
    Long getTotalLessons();

    default double getPercentage() {
        long total = getTotalLessons() == null
                ? 0
                : getTotalLessons();

        long completed = getCompletedLessons() == null
                ? 0
                : getCompletedLessons();

        return total == 0
                ? 0.0
                : completed * 100.0 / total;
    }
}