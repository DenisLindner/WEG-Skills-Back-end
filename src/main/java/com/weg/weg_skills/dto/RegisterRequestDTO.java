package com.weg.weg_skills.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
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
        String email,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 72,
                message = "Password must contain between 8 and 72 characters"
        )
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "Password must contain an uppercase letter, a lowercase letter, a number, and a special character"
        )
        String password
) {
}
