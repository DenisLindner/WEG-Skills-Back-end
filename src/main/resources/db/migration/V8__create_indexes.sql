CREATE INDEX idx_medias_user_id
    ON medias (user_id);

CREATE INDEX idx_courses_instructor_id
    ON courses (instructor_id);

CREATE INDEX idx_modules_course_id
    ON modules (course_id);

CREATE INDEX idx_lessons_module_id
    ON lessons (module_id);

CREATE INDEX idx_enrollments_course_id
    ON enrollments (course_id);

CREATE INDEX idx_reviews_course_id
    ON reviews (course_id);