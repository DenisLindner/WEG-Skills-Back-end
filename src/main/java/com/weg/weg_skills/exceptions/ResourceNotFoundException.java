package com.weg.weg_skills.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource+" with id: "+id+", not found");
    }
}
