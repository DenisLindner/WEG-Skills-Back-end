package com.weg.weg_skills.service;

import com.weg.weg_skills.dto.UserCreateRequestDTO;
import com.weg.weg_skills.dto.UserResponseDTO;
import com.weg.weg_skills.mapper.UserMapper;
import com.weg.weg_skills.model.User;
import com.weg.weg_skills.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository userRepository;
    private UserMapper userMapper;

    public UserResponseDTO create(UserCreateRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new RuntimeException();
        }

        User user = userMapper.toEntity(dto);

        user = userRepository.save(user);

        return userMapper.toResponse(user);
    }
}
