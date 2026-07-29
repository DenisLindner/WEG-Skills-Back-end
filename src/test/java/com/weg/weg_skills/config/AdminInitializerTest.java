package com.weg.weg_skills.config;

import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminInitializerTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ApplicationArguments arguments;

    @Test
    void shouldCreateAdminWithNormalizedEmail() {
        AdminInitializer initializer = initializer(
                "System Administrator",
                "  ADMIN@EXAMPLE.COM ",
                "Strong1!"
        );

        when(userRepository.findByEmailIgnoreCase("admin@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("Strong1!")).thenReturn("encoded-password");

        initializer.run(arguments);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User admin = captor.getValue();
        assertThat(admin.getName()).isEqualTo("System Administrator");
        assertThat(admin.getEmail()).isEqualTo("admin@example.com");
        assertThat(admin.getPassword()).isEqualTo("encoded-password");
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void shouldSkipInitializationWhenEmailIsBlank() {
        AdminInitializer initializer = initializer(
                "System Administrator",
                " ",
                "Strong1!"
        );

        initializer.run(arguments);

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldSkipInitializationWhenPasswordIsBlank() {
        AdminInitializer initializer = initializer(
                "System Administrator",
                "admin@example.com",
                " "
        );

        initializer.run(arguments);

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldKeepExistingAdmin() {
        AdminInitializer initializer = initializer(
                "System Administrator",
                "admin@example.com",
                "Strong1!"
        );
        User existingAdmin = new User(
                "Existing Administrator",
                "admin@example.com",
                "existing-password",
                UserRole.ADMIN
        );

        when(userRepository.findByEmailIgnoreCase("admin@example.com"))
                .thenReturn(Optional.of(existingAdmin));

        initializer.run(arguments);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldRejectExistingNonAdminUser() {
        AdminInitializer initializer = initializer(
                "System Administrator",
                " ADMIN@EXAMPLE.COM ",
                "Strong1!"
        );
        User existingStudent = new User(
                "Existing Student",
                "admin@example.com",
                "existing-password",
                UserRole.STUDENT
        );

        when(userRepository.findByEmailIgnoreCase("admin@example.com"))
                .thenReturn(Optional.of(existingStudent));

        assertThatThrownBy(() -> initializer.run(arguments))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("admin@example.com");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    private AdminInitializer initializer(
            String name,
            String email,
            String password
    ) {
        return new AdminInitializer(
                userRepository,
                passwordEncoder,
                name,
                email,
                password
        );
    }
}
