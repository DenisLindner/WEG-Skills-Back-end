package com.weg.weg_skills.exceptions;

public class EnrollmentNotFoundException extends RuntimeException {
    public EnrollmentNotFoundException(Long courseId, Long userId) {
        super("Enrollment with course id: "+courseId+", and user id: "+userId+", not found");
    }
}
