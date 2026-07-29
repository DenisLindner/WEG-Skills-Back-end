package com.weg.weg_skills.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterInstructorRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(
                min = 3,
                max = 128,
                message = "Name must contain between 3 and 128 characters"
        )
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email")
        @Size(max = 128)
        String email
) {
}
