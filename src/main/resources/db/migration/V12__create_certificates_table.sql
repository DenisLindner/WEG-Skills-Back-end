CREATE TABLE certificates (
    code VARCHAR(50),

    student_name VARCHAR(128) NOT NULL,
    course_title VARCHAR(128) NOT NULL,
    total_lessons BIGINT NOT NULL,

    course_id BIGINT,
    user_id BIGINT,

    completed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_certificates
        PRIMARY KEY (code),

    CONSTRAINT uk_certificate_course_user
        UNIQUE (course_id, user_id),

    CONSTRAINT fk_certificates_course
        FOREIGN KEY (course_id)
            REFERENCES courses (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_certificates_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);