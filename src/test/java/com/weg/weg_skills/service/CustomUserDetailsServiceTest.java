package com.weg.weg_skills.service;

import com.weg.weg_skills.TestData;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks CustomUserDetailsService service;

    @Test
    void shouldLoadUserByTrimmedEmail() {
        User user = TestData.user(1L, UserRole.STUDENT);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

        assertThat(service.loadUserByUsername(" user@example.com ")).isSameAs(user);
        verify(userRepository).findByEmailIgnoreCase("user@example.com");
    }

    @Test
    void shouldFailWhenUserDoesNotExist() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
