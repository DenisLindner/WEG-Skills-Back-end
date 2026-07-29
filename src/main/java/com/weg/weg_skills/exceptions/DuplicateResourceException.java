package com.weg.weg_skills.exceptions;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String entity, String resource, String value) {
        super(entity+" already uses this "+resource+": "+value);
    }
}
