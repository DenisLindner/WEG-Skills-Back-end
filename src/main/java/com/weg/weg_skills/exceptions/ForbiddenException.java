package com.weg.weg_skills.exceptions;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("You don't have permission for this action");
    }
}
