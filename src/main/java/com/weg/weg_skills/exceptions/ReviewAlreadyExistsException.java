package com.weg.weg_skills.exceptions;

public class ReviewAlreadyExistsException extends RuntimeException {
    public ReviewAlreadyExistsException() {
        super("Review already exists with this course and user");
    }
}
