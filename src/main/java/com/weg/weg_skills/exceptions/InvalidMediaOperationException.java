package com.weg.weg_skills.exceptions;

public class InvalidMediaOperationException extends RuntimeException {
    public InvalidMediaOperationException() {
        super("Invalid media type");
    }
}
