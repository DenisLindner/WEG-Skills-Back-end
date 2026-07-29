package com.weg.weg_skills.model;

import com.weg.weg_skills.enums.MediaStatus;
import com.weg.weg_skills.enums.MediaType;
import com.weg.weg_skills.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainModelTest {

    @Test
    void shouldChangeMediaStatus() {
        User owner = new User("User", "user@example.com", "password", UserRole.STUDENT);
        Media media = new Media("bucket", "key", "file.png", "image/png", 100,
                MediaType.USER_IMAGE, MediaStatus.PENDING_UPLOAD, owner);

        assertThat(media.isPendingUpload()).isTrue();

        media.markAsReady(100L);

        assertThat(media.isReady()).isTrue();
        assertThat(media.getActualSize()).isEqualTo(100L);

        media.markAsFailed(90L);

        assertThat(media.getMediaStatus()).isEqualTo(MediaStatus.FAILED);
        assertThat(media.getActualSize()).isEqualTo(90L);

        media.setMediaStatus(MediaStatus.DELETED);
        assertThat(media.isDeleted()).isTrue();
    }

    @Test
    void shouldExposeUserDetails() {
        User user = new User("Admin", "admin@example.com", "encoded", UserRole.ADMIN);

        assertThat(user.getUsername()).isEqualTo("admin@example.com");
        assertThat(user.getPassword()).isEqualTo("encoded");
        assertThat(user.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
        assertThat(UserRole.STUDENT.asAuthority()).isEqualTo("ROLE_STUDENT");
    }
}
