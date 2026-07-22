package com.weg.weg_skills.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequestDTO(
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
        String password,

        @NotBlank(message = "Actual password is required")
        @Size(
                max = 72,
                message = "Current password must contain at most 72 characters"
        )
        String actualPassword
) {
}
