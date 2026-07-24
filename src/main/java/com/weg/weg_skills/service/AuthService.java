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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@AllArgsConstructor
public class AuthService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UserMapper userMapper;
    private AuthMapper authMapper;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        String email = normalizeEmail(dto.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("User", "email", email);
        }

        User user = userMapper.toEntity(dto.name(), email, passwordEncoder.encode(dto.password()), UserRole.STUDENT);

        userRepository.save(user);

        JwtService.GeneratedToken generatedToken =
                jwtService.generateToken(
                        user.getUsername(),
                        user.getId(),
                        user.getAuthorities()
                );

        log.atInfo().addKeyValue("email", email).addKeyValue("role", UserRole.STUDENT).log("User registered");

        return authMapper.toResponse(generatedToken.value(), "Bearer", generatedToken.expiresAt());
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO dto) {
        String email = normalizeEmail(dto.email());

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    dto.password()
                            )
                    );

            User user = (User) authentication.getPrincipal();

            if (user == null) {
                throw new UnauthorizedException();
            }

            JwtService.GeneratedToken generatedToken =
                    jwtService.generateToken(
                            authentication.getName(),
                            user.getId(),
                            authentication.getAuthorities()
                    );

            return authMapper.toResponse(generatedToken.value(), "Bearer", generatedToken.expiresAt());

        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
