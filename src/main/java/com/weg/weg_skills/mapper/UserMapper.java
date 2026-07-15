package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.UserCreateRequestDTO;
import com.weg.weg_skills.dto.UserResponseDTO;
import com.weg.weg_skills.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserCreateRequestDTO dto) {
        return new User(dto.name(), dto.email(), dto.password());
    }

    public UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getBirthday(), user.getPhone(), user.getPictureUrl());
    }
}
