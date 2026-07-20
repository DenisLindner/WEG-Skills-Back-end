package com.weg.weg_skills.exceptions;

public class StorageServiceException extends RuntimeException {
    public StorageServiceException(String message, Exception ex) {
        super(message, ex);
    }
}
