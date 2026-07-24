package com.weg.weg_skills;

import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.model.Course;
import com.weg.weg_skills.model.Lesson;
import com.weg.weg_skills.model.Media;
import com.weg.weg_skills.model.Module;
import com.weg.weg_skills.model.User;

public final class TestData {

    private TestData() {
    }

    public static User user(Long id, UserRole role) {
        User user = new User("Test User", "user@example.com", "encoded-password", role);
        user.setId(id);
        return user;
    }

    public static Course course(Long id, User instructor) {
        Course course = new Course("Java Basics", "Course description", instructor);
        course.setId(id);
        return course;
    }

    public static Module module(Long id, Course course) {
        Module module = new Module("First Module", "Module description", course);
        module.setId(id);
        return module;
    }

    public static Lesson lesson(Long id, Module module) {
        Lesson lesson = new Lesson("First Lesson", "Lesson description", module);
        lesson.setId(id);
        return lesson;
    }

    public static Media media(Long id, User owner, MediaType type, MediaStatus status) {
        String contentType = type == MediaType.LESSON_VIDEO ? "video/mp4" : "image/png";
        String bucket = type == MediaType.LESSON_VIDEO ? "private-videos" : "public-assets";
        Media media = new Media(bucket, "objects/key", "file", contentType, 100L, type, status, owner);
        media.setId(id);
        return media;
    }
}
