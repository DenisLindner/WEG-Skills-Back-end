package com.weg.weg_skills.exceptions;

public class EnrollmentAlreadyExistsException extends RuntimeException {
    public EnrollmentAlreadyExistsException() {
        super("Enrollment already exists with this course and user");
    }
}
