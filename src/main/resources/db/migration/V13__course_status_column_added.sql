ALTER TABLE courses
    ADD COLUMN
        course_status VARCHAR(32) NOT NULL DEFAULT 'DRAFT';

ALTER TABLE courses
    ADD CONSTRAINT ck_courses_status
        CHECK (
            course_status IN (
                              'DRAFT',
                              'PUBLISHED'
                )
            );

CREATE INDEX idx_course_status
    ON courses (course_status);