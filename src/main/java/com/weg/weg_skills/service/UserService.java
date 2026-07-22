package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.*;
import com.weg.weg_skills.exceptions.*;
import com.weg.weg_skills.mapper.EnrollmentMapper;
import com.weg.weg_skills.mapper.UserMapper;
import com.weg.weg_skills.model.Enrollment;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.EnrollmentRepository;
import com.weg.weg_skills.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository userRepository;
    private UserMapper userMapper;
    private MediaService mediaService;
    private PasswordEncoder passwordEncoder;
    private EnrollmentRepository enrollmentRepository;
    private EnrollmentMapper enrollmentMapper;

    @Transactional(readOnly = true)
    public UserResponseDTO getMeProfile(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

        return userMapper.toResponse(user, user.getImage() != null && user.getImage().isReady() ? mediaService.getPublicUrl(user.getImage().getId()) : null);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponseDTO> getMeEnrollments(Long id, int page, int size) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException();
        }

        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by("createdAt").descending()
        );

        Page<Enrollment> enrollments = enrollmentRepository.findAllByUser(user, pageable);

        return enrollments.map(e -> enrollmentMapper.toResponseDTO(e));
    }

    @Transactional
    public UploadTicketResponseDTO uploadImage(Long id, CreateMediaUploadRequestDTO dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

        CreatedMediaUpload createdMedia = mediaService.createUserImageUpload(id, dto);

        if (user.getImage() != null) {
            mediaService.delete(user.getImage().getId());
        }
        user.setImage(createdMedia.media());
        userRepository.save(user);

        return createdMedia.ticket();
    }

    @Transactional
    public UserResponseDTO update(Long id, UserUpdateRequestDTO dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (dto.name() != null && !dto.name().trim().isEmpty()) {
            String name = normalizeString(dto.name());
            user.setName(name);
        }
        if (dto.email() != null && !dto.email().trim().isEmpty()) {
            String email = normalizeEmail(dto.email());

            if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmailIgnoreCase(email)) {
                throw new DuplicateResourceException("User", "email", email);
            }

            user.setEmail(email);
        }

        if (dto.birthday() != null) {
            user.setBirthday(dto.birthday());
        }

        if (dto.phone() != null && !dto.phone().trim().isEmpty()) {
            String phone = normalizeString(dto.phone());
            user.setPhone(phone);
        }

        if (dto.city() != null && !dto.city().trim().isEmpty()) {
            String city = normalizeString(dto.city());
            user.setCity(city);
        }

        if (dto.state() != null && !dto.state().trim().isEmpty()) {
            String state = normalizeString(dto.state());
            user.setState(state);
        }

        if (dto.country() != null && !dto.country().trim().isEmpty()) {
            String country = normalizeString(dto.country());
            user.setCountry(country);
        }

        user = userRepository.save(user);

        return userMapper.toResponse(user, user.getImage() != null && user.getImage().isReady() ? mediaService.getPublicUrl(user.getImage().getId()) : null);
    }

    @Transactional
    public void changePassword(Long id, PasswordChangeRequestDTO dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!passwordEncoder.matches(dto.actualPassword(), user.getPassword())) {
            throw new UnauthorizedException();
        }

        user.setPassword(passwordEncoder.encode(dto.password()));

        userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!user.getCourses().isEmpty()) {
            throw new UserHasCoursesException();
        }

        if (user.getImage() != null) {
            mediaService.delete(user.getImage().getId());
        }

        userRepository.deleteById(id);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeString(String value) {
        return value.trim();
    }
}
