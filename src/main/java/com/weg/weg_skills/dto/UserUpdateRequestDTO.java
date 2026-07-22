package com.weg.weg_skills.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserUpdateRequestDTO(
        @Size(
                min = 3,
                max = 128,
                message = "Name must contain between 3 and 128 characters"
        )
        String name,

        @Email(message = "Email must be a valid email")
        String email,

        @Past(message = "Birthday must be a day in the past")
        LocalDate birthday,

        @Size(
                min = 7,
                max = 20,
                message = "Phone must contain between 7 and 20 characters"
        )
        String phone,

        @Size(
                min = 3,
                max = 128,
                message = "City must contain between 3 and 128 characters"
        )
        String city,

        @Size(
                min = 3,
                max = 128,
                message = "State must contain between 3 and 128 characters"
        )
        String state,

        @Size(
                min = 3,
                max = 128,
                message = "Country must contain between 3 and 128 characters"
        )
        String country
) {
}
