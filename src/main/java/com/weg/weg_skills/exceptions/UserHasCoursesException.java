package com.weg.weg_skills.exceptions;

public class UserHasCoursesException extends RuntimeException {
    public UserHasCoursesException() {
        super("User cannot be deleted while assigned courses exist");
    }
}
