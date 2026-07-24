package com.weg.weg_skills.config;

import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
public class AdminInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final String name;
    private final String email;
    private final String password;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                            @Value("${ADMIN_NAME:Administrator}") String name,
                            @Value("${ADMIN_EMAIL:}") String email,
                            @Value("${ADMIN_PASSWORD:}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        userRepository.findByEmailIgnoreCase(normalizedEmail)
                .ifPresentOrElse(
                        user -> validateExistingUser(user, normalizedEmail),
                        () -> createAdmin(normalizedEmail)
                );
    }

    private void createAdmin(String normalizedEmail) {
        User admin = new User(
                name,
                normalizedEmail,
                passwordEncoder.encode(password),
                UserRole.ADMIN
        );

        userRepository.save(admin);
    }

    private void validateExistingUser(User user, String normalizedEmail) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException(
                    "A non-admin user already exists with email " + normalizedEmail
            );
        }
    }
}
