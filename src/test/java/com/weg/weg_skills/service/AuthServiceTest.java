package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.AuthResponseDTO;
import com.weg.weg_skills.dto.LoginRequestDTO;
import com.weg.weg_skills.dto.RegisterRequestDTO;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.exceptions.DuplicateResourceException;
import com.weg.weg_skills.exceptions.InvalidCredentialsException;
import com.weg.weg_skills.exceptions.UnauthorizedException;
import com.weg.weg_skills.mapper.AuthMapper;
import com.weg.weg_skills.mapper.UserMapper;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock UserMapper userMapper;
    @Mock AuthMapper authMapper;
    @InjectMocks AuthService service;

    @Test
    void shouldRegisterStudentWithNormalizedEmail() {
        RegisterRequestDTO request = new RegisterRequestDTO("John", "  JOHN@EXAMPLE.COM ", "Strong1!");
        User user = new User("John", "john@example.com", "encoded", UserRole.STUDENT);
        user.setId(1L);
        Instant expiration = Instant.parse("2030-01-01T00:00:00Z");
        AuthResponseDTO expected = new AuthResponseDTO("token", "Bearer", expiration);

        when(passwordEncoder.encode("Strong1!")).thenReturn("encoded");
        when(userMapper.toEntity("John", "john@example.com", "encoded", UserRole.STUDENT)).thenReturn(user);
        when(jwtService.generateToken(eq("john@example.com"), eq(1L), any())).thenReturn(new JwtService.GeneratedToken("token", expiration));
        when(authMapper.toResponse("token", "Bearer", expiration)).thenReturn(expected);

        AuthResponseDTO response = service.register(request);

        assertThat(response).isEqualTo(expected);
        verify(userRepository).existsByEmailIgnoreCase("john@example.com");
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectDuplicatedRegistrationEmail() {
        RegisterRequestDTO request = new RegisterRequestDTO("John", " JOHN@EXAMPLE.COM ", "Strong1!");
        when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request)).isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginWithNormalizedEmail() {
        LoginRequestDTO request = new LoginRequestDTO(" USER@EXAMPLE.COM ", "password");
        User user = new User("User", "user@example.com", "encoded", UserRole.STUDENT);
        user.setId(7L);
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                user, "password", user.getAuthorities());
        Instant expiration = Instant.parse("2030-01-01T00:00:00Z");
        AuthResponseDTO expected = new AuthResponseDTO("token", "Bearer", expiration);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(eq("user@example.com"), eq(7L), any()))
                .thenReturn(new JwtService.GeneratedToken("token", expiration));
        when(authMapper.toResponse("token", "Bearer", expiration)).thenReturn(expected);

        assertThat(service.login(request)).isEqualTo(expected);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("user@example.com");
    }

    @Test
    void shouldRejectInvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("invalid"));

        assertThatThrownBy(() -> service.login(new LoginRequestDTO("user@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldRejectAuthenticationWithoutPrincipal() {
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);

        assertThatThrownBy(() -> service.login(new LoginRequestDTO("user@example.com", "password")))
                .isInstanceOf(UnauthorizedException.class);
    }
}
