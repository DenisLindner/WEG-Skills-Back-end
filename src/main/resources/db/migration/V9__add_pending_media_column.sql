ALTER TABLE users
    ADD COLUMN pending_image_media_id BIGINT;

ALTER TABLE courses
    ADD COLUMN pending_image_media_id BIGINT;

ALTER TABLE modules
    ADD COLUMN pending_image_media_id BIGINT;

ALTER TABLE lessons
    ADD COLUMN pending_video_media_id BIGINT;


ALTER TABLE users
    ADD CONSTRAINT uk_users_pending_image_media
        UNIQUE (pending_image_media_id),
    ADD CONSTRAINT fk_users_pending_image_media
        FOREIGN KEY (pending_image_media_id)
        REFERENCES medias (id)
        ON DELETE SET NULL;


ALTER TABLE courses
    ADD CONSTRAINT uk_courses_pending_image_media
        UNIQUE (pending_image_media_id),
    ADD CONSTRAINT fk_courses_pending_image_media
        FOREIGN KEY (pending_image_media_id)
        REFERENCES medias (id)
        ON DELETE SET NULL;


ALTER TABLE modules
    ADD CONSTRAINT uk_modules_pending_image_media
        UNIQUE (pending_image_media_id),
    ADD CONSTRAINT fk_modules_pending_image_media
        FOREIGN KEY (pending_image_media_id)
        REFERENCES medias (id)
        ON DELETE SET NULL;


ALTER TABLE lessons
    ADD CONSTRAINT uk_lessons_pending_video_media
        UNIQUE (pending_video_media_id),
    ADD CONSTRAINT fk_lessons_pending_video_media
        FOREIGN KEY (pending_video_media_id)
        REFERENCES medias (id)
        ON DELETE SET NULL;

ALTER TABLE users
    ADD COLUMN version INTEGER NOT NULL DEFAULT 0;

ALTER TABLE courses
    ADD COLUMN version INTEGER NOT NULL DEFAULT 0;

ALTER TABLE modules
    ADD COLUMN version INTEGER NOT NULL DEFAULT 0;

ALTER TABLE lessons
    ADD COLUMN version INTEGER NOT NULL DEFAULT 0;

ALTER TABLE medias
    ADD COLUMN version INTEGER NOT NULL DEFAULT 0;