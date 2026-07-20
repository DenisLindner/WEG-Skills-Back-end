package com.weg.weg_skills.mapper;

import com.weg.weg_skills.dto.UserResponseDTO;
import com.weg.weg_skills.enums.UserRole;
import com.weg.weg_skills.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(String name, String email, String passwordHash, UserRole role) {
        return new User(name, email, passwordHash, role);
    }

    public UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getBirthday(), user.getPhone(), user.getPictureUrl());
    }
}
